package uni.tubingen.fido;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * This is the model implementation of FidoProteinInference.
 * 
 *
 * @author enrique
 */
public class FidoProteinInferenceNodeModel extends NodeModel {
    
	 // the logger instance
   
	private static final NodeLogger logger = NodeLogger
            .getLogger("Protein Fido probabilities");
	
	static final String CFGKEY_PEPTIDES = "peptides";
	static final String CFGKEY_PROTEIN  = "protein";
	static final String CFGKEY_ALGO     = "algorithm";

    static final String CFGKEY_PROBA = "probabilities";

	static final String CFGKEY_ALFA_PARAMETER = "alfa";
	static final String CFGKEY_GAMMA_PARAMETER = "gamma";
	static final String CFGKEY_BETA_PARAMETER = "beta";
	
	
	private final SettingsModelDoubleBounded alfa_parameter = new SettingsModelDoubleBounded(CFGKEY_ALFA_PARAMETER, 0.9, 0.0, 1.0);
	private final SettingsModelDoubleBounded beta_parameter = new SettingsModelDoubleBounded(CFGKEY_BETA_PARAMETER, 0.5, 0.0, 1.0);
	private final SettingsModelDoubleBounded gamma_parameter = new SettingsModelDoubleBounded(CFGKEY_GAMMA_PARAMETER, 0.05, 0.0, 1.0);
	
	private final SettingsModelString m_peptide_column = new SettingsModelString(CFGKEY_PEPTIDES, "Peptides");
	private final SettingsModelString m_accsn_column   = new SettingsModelString(CFGKEY_PROTEIN, "Protein");
	private final SettingsModelString m_proba_column   = new SettingsModelString(CFGKEY_PROBA, "Probabilities");
	//private final SettingsModelString m_algorithm      = new SettingsModelString(CFGKEY_ALGO, "ILP: Minimum Set Cover");


	/**
     * Constructor for the node model.
     */
    protected FidoProteinInferenceNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
 

    	logger.info("Minimising protein set for "+inData[0].getRowCount()+" rows in input dataset.");
    	
    	int pep_idx  = inData[0].getDataTableSpec().findColumnIndex(m_peptide_column.getStringValue());
    	int accsn_idx= inData[0].getDataTableSpec().findColumnIndex(m_accsn_column.getStringValue());
    	int proba_idx= inData[0].getDataTableSpec().findColumnIndex(m_proba_column.getStringValue());
    	
    	
    	if (pep_idx < 0 || accsn_idx < 0 || pep_idx == accsn_idx) {
    		throw new Exception("Illegal columns: "+ m_peptide_column+" "+m_accsn_column+" "+m_proba_column+", re-configure the node!");
    	}
    	
   
    	//version2 
    	DataTableSpec new_spec_table = new DataTableSpec(new_make_output_spec());  	
    	BufferedDataContainer container = exec.createDataContainer(new_spec_table);
    	
    	
    	//getting row iteretor for data table incoming
    	RowIterator row_it = inData[0].iterator();
    	
    	HashMap<String,String> prot2pep = new HashMap<String,String>();
    	HashMap<String,Double> pep2proba = new HashMap<String, Double>();
    	HashMap<String,String[]> pep2protein_group = new HashMap<String, String[]>();
    	
  
    	
    	logger.info("Processing raw input rows");
    	
    	//building the protein--->peptide and peptide--->probabilities map
    	
    	while (row_it.hasNext()) {
    		
    		DataRow r = row_it.next();
    		DataCell pep_cell =  r.getCell(pep_idx);
    		DataCell accsn_cell= r.getCell(accsn_idx);
    		DataCell proba_cell= r.getCell(proba_idx);
    		
    		
    		// rows with missing cells cannot be processed (no missing values in PSM graph...)
    		if (pep_cell.isMissing() || accsn_cell.isMissing() || proba_cell.isMissing()) {
    			continue;
    		}
    		
    		String peptide_entry   = ((StringValue)pep_cell).getStringValue();
    		Double proba_entry   =   Double.parseDouble(((StringValue) proba_cell).getStringValue());
    		String protein_group   = ((StringValue)accsn_cell).getStringValue();
    		
    		     if(pep2proba.containsKey(peptide_entry)) {  //avoid peptide redundancy		
    		  
    		          if(pep2proba.get(peptide_entry) < (Double)proba_entry)  //getting highest probability for peptide
    			         pep2proba.put(peptide_entry, proba_entry);
    		             pep2protein_group.put(peptide_entry, getProteinGroup(protein_group));
    	
    		      }else{
    		
    	           pep2proba.put(peptide_entry, proba_entry);
    	           pep2protein_group.put(peptide_entry, getProteinGroup(protein_group));	
    		      }
    		 
    		
    	  }
    	
    	// System.out.println(prot2pep);
    	
    
    	
    	//writting psm non-redundante temporal file from prot2pep map and pep2proba map
    	
    	Set<String> set_pep = pep2proba.keySet();
    	Set<String> set_pro = prot2pep.keySet();
    	String [] match_proteins; //protein list for same peptide----could be one protein 
    	

    	
    	//creating psm graph file using peptide---->protein_group map (FIDO input file)
    	
    	File  new_tmp_psm_graph_file = File.createTempFile("new_psm_graph_file", ".txt");
    	PrintWriter new_pwriter = new PrintWriter(new FileWriter(new_tmp_psm_graph_file));
    	for (String sequence: set_pep){
    		//int i = 0;
    	    new_pwriter.println("e "+sequence);
    		for (int i=0; i < pep2protein_group.get(sequence).length; i++){
    			new_pwriter.println("r "+ pep2protein_group.get(sequence)[i]);
    	    	}
        	 
    	  new_pwriter.println("p "+ pep2proba.get(sequence).toString());	
    	//  match_protein.clear();
    	}
    	
    	new_pwriter.close();
    	System.out.println(new_tmp_psm_graph_file.getPath());
    
    	
    	logger.info("Created PSM graph file: " + new_tmp_psm_graph_file.getAbsolutePath());
    	
    	logger.info("Running Fido to compute protein probabilities...");
    	
    	// run Fido to compute a solution... (hopefully!)
    	
    	String alfa_value = Double.valueOf(this.alfa_parameter.getDoubleValue()).toString();
    	String beta_value = Double.valueOf(this.beta_parameter.getDoubleValue()).toString();
    	String gamma_value = Double.valueOf(this.gamma_parameter.getDoubleValue()).toString();
    	
    	System.out.println(alfa_value);
    	System.out.println(beta_value);
    	System.out.println(gamma_value);
    	
    	FidoProcess process = FidoProcess.getInstance(new_tmp_psm_graph_file.getAbsolutePath(),
                alfa_value, beta_value, gamma_value);

    	HashMap<String, String> protein2p_fido = process.computeProteinInference();
    	
    	
    	// 3. output probability for those rows containing protein accesion ID
    	if (!protein2p_fido.isEmpty()) {
	    	
	      Set<String> protein_IDs_set = protein2p_fido.keySet();
	    	
	    	for (String protein_ID : protein_IDs_set) {
	    		
	    		String input_acc_protein = FidoGlobal.INTERNAL_MAP_ID.get(Integer.valueOf(protein_ID));
	    		
	    		if (protein_ID != null && !protein_ID.isEmpty() && input_acc_protein != null){
	    			
	    		RowKey key = new RowKey(protein_ID);
                DataCell[] cells = new DataCell[2];	    		
	    		String p_fido = protein2p_fido.get(protein_ID);
	    		cells[0] = new StringCell(formattedProteinAccesion(input_acc_protein));
	    		cells[1] = new StringCell(p_fido);
	    		DataRow row = new DefaultRow(key, cells);
	    		//System.out.println(Integer.valueOf(protein_ID) + "   " + FidoGlobal.INTERNAL_MAP_ID.get(Integer.valueOf(protein_ID)));
	    		container.addRowToTable(row);
	    		
	    		}
	    	  }
    	   } 
    	   else 
    	   { 
    	     throw new Exception("output fido is empty...");
    	   }
    	
    	
    	 container.close();
    	return new BufferedDataTable[] { container.getTable() };
    }
    


    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: generated method stub
    	 return new DataTableSpec[]{new DataTableSpec(this.new_make_output_spec())};
    }
    
    
    private DataColumnSpec[] new_make_output_spec() {
    	
    	DataColumnSpec cols[] = new DataColumnSpec[2];
    	cols[0] = new DataColumnSpecCreator("Protein ID", StringCell.TYPE).createSpec();
    	cols[1] = new DataColumnSpecCreator("Fido Probability", StringCell.TYPE).createSpec();
	
      return cols;
	}
    
    //methods for getting protein group (because some time is possible this situation: peptide ---> multiple protein entry)
    private static String[] getProteinGroup (String protein_entries){
    	
    	String [] prot_ID_group;
    	String [] only_protein = new String[1];
    	if (protein_entries.contains(";")){
    		prot_ID_group = protein_entries.split(";");
		    return formattedProteinAccesion(prot_ID_group);
    	}        
		else{
			only_protein[0] = protein_entries;
			return formattedProteinAccesion(only_protein);
		} 
    }
    
    //this method convert the protein name in a simple identifier (internal)...avoiding protein name with space (trouble!!!!)
    private static String[] formattedProteinAccesion(String[] protein_group){
    	String[] formatted_protein = new String [protein_group.length];
    	for(int i=0; i < protein_group.length; i++){
    		String acc = protein_group[i];
    		String acc_code = getInternalIdProtein (acc);
    		formatted_protein[i] = acc_code;
    	}
    return formatted_protein;
    }
    
	static private String formattedProteinAccesion (String protein_accn){
	      if(protein_accn.contains("|")){
	    	  return StringUtils.substringBetween(protein_accn, "|");
	      }
	      else{
	    	  return protein_accn; 
	      }
	  		
      }
    
    //method for getting the internal identifier used during excecuiton...(partial solution in this moment)
    private static String getInternalIdProtein (String protein_id){
    	FidoGlobal.INTERNAL_ID_PROTEIN ++; 
    	FidoGlobal.INTERNAL_MAP_ID.put(FidoGlobal.INTERNAL_ID_PROTEIN, protein_id);
      return String.valueOf(FidoGlobal.INTERNAL_ID_PROTEIN);
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_peptide_column.saveSettingsTo(settings);
        m_accsn_column.saveSettingsTo(settings);
        m_proba_column.saveSettingsTo(settings);

        alfa_parameter.saveSettingsTo(settings);
        beta_parameter.saveSettingsTo(settings);
        gamma_parameter.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	 m_peptide_column.loadSettingsFrom(settings);
         m_accsn_column.loadSettingsFrom(settings);
         m_proba_column.loadSettingsFrom(settings);
         
         alfa_parameter.loadSettingsFrom(settings);
         beta_parameter.loadSettingsFrom(settings);
         gamma_parameter.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	 m_peptide_column.validateSettings(settings);
         m_accsn_column.validateSettings(settings);
         m_proba_column.validateSettings(settings);
         
        alfa_parameter.validateSettings(settings);
        beta_parameter.validateSettings(settings);
        gamma_parameter.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

