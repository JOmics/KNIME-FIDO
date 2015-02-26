package uni.tubingen.inference.fido;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
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

	static final String CFGKEY_CHOOSE_PARAMETERS = "choose";
	static final String CFGKEY_DECOY_LABEL = "decoy_label";
	
	static final String CFGKEY_ALPHA_PARAMETER = "alpha";
	static final String CFGKEY_BETA_PARAMETER = "beta";
	static final String CFGKEY_GAMMA_PARAMETER = "gamma";
	
	private final SettingsModelBoolean choose_parameters = new SettingsModelBoolean(CFGKEY_CHOOSE_PARAMETERS, true);
	private final SettingsModelString decoy_label_parameter = new SettingsModelString(CFGKEY_DECOY_LABEL, "REVERSED");
	
	private final SettingsModelDoubleBounded alpha_parameter = new SettingsModelDoubleBounded(CFGKEY_ALPHA_PARAMETER, 0.1, 0.0, 1.0);
	private final SettingsModelDoubleBounded beta_parameter  = new SettingsModelDoubleBounded(CFGKEY_BETA_PARAMETER, 0.01, 0.0, 1.0);
	private final SettingsModelDoubleBounded gamma_parameter = new SettingsModelDoubleBounded(CFGKEY_GAMMA_PARAMETER, 0.5, 0.0, 1.0);
	
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
    	
    	HashMap<String, Double> pep2proba = new HashMap<String, Double>();
    	HashMap<String, List<Integer>> pep2proteins = new HashMap<String, List<Integer>>();
    	
    	// a list containing all the names, internally the index is used for calling Fido
    	List<String> proteinLongnames = new ArrayList<String>();
    	
    	HashSet<Integer> targets = new HashSet<Integer>();
    	HashSet<Integer> decoys = new HashSet<Integer>();
    	String decoyLabel = this.decoy_label_parameter.getStringValue();
    	
    	
    	logger.info("Processing raw input rows");
    	
    	//building the protein--->peptide and peptide--->probabilities map
    	
    	while (row_it.hasNext()) {
    		DataRow r = row_it.next();
    		DataCell pep_cell   = r.getCell(pep_idx);
    		DataCell accsn_cell = r.getCell(accsn_idx);
    		DataCell proba_cell = r.getCell(proba_idx);
    		
    		// rows with missing cells cannot be processed (no missing values in PSM graph...)
    		if (pep_cell.isMissing() || accsn_cell.isMissing() || proba_cell.isMissing()) {
    			continue;
    		}
    		
    		String peptide_entry = ((StringValue)pep_cell).getStringValue();
    		Double proba_entry   = ((DoubleValue)proba_cell).getDoubleValue();
    		String[] protein_names = ((StringValue)accsn_cell).getStringValue().split(";");
    		
    		List<Integer> proteinsIDs = new ArrayList<Integer>();
    		for (String proteinName : protein_names) {
    			if (!proteinLongnames.contains(proteinName)) {
    				proteinLongnames.add(proteinName);
    			}
    			int idx = proteinLongnames.indexOf(proteinName);
    			proteinsIDs.add(idx);
    			
    			if (proteinName.contains(decoyLabel)) {
    				decoys.add(idx);
    			} else {
    				targets.add(idx);
    			}
    		}
    		
    		if (!pep2proba.containsKey(peptide_entry) ||
    				(pep2proba.get(peptide_entry) < proba_entry)) {  //avoid peptide redundancy
				pep2proba.put(peptide_entry, proba_entry);
				pep2proteins.put(peptide_entry, proteinsIDs);
    		}
    	}
    	// System.out.println(prot2pep);
    
    	
    	//writting psm non-redundante temporal file from prot2pep map and pep2proba map
    	File new_tmp_psm_graph_file = File.createTempFile("new_psm_graph_file", ".txt");
    	PrintWriter new_pwriter = new PrintWriter(new FileWriter(new_tmp_psm_graph_file));
    	for (Map.Entry<String, Double> pepIt : pep2proba.entrySet()) {
    		new_pwriter.println("e " + pepIt.getKey());
    		
    		for (Integer protID : pep2proteins.get(pepIt.getKey())) {
        		new_pwriter.println("r " + protID);	
    		}
    		
    		new_pwriter.println("p " + pepIt.getValue());	
    	}
    	new_pwriter.close();
    	logger.info("Created PSM graph file: " + new_tmp_psm_graph_file.getAbsolutePath());
    	
    	File new_tmp_targetdecoy_file = File.createTempFile("new_targetdecoy_file", ".txt");
    	new_pwriter = new PrintWriter(new FileWriter(new_tmp_targetdecoy_file));
    	new_pwriter.append("{ ");
    	int c = 0;
    	for (Integer prot : targets) {
    		if (c++ > 0) {
    			new_pwriter.append(" , ");	
    		}
    		new_pwriter.append(prot.toString());
    	}
    	new_pwriter.append(" }");
    	new_pwriter.println();
    	new_pwriter.append("{ ");
    	c = 0;
    	for (Integer prot : decoys) {
    		if (c++ > 0) {
    			new_pwriter.append(" , ");	
    		}
    		new_pwriter.append(prot.toString());
    	}
    	new_pwriter.append(" }");
    	new_pwriter.close();
    	logger.info("Created target-decoy file: " + new_tmp_targetdecoy_file.getAbsolutePath());
    	
    	
    	logger.info("Running Fido to compute protein probabilities...");
    	
    	// run Fido to compute a solution... (hopefully!)
    	HashMap<List<String>, String> protein2p_fido = null;
    	if (this.choose_parameters.getBooleanValue()) {
        	FidoChooseParametersProcess process =
        			new FidoChooseParametersProcess(new_tmp_psm_graph_file.getAbsolutePath(), new_tmp_targetdecoy_file.getAbsolutePath(), true); 
        	protein2p_fido = process.computeProteinInference();
    	} else {
        	String alpha_value = Double.valueOf(this.alpha_parameter.getDoubleValue()).toString();
        	String beta_value = Double.valueOf(this.beta_parameter.getDoubleValue()).toString();
        	String gamma_value = Double.valueOf(this.gamma_parameter.getDoubleValue()).toString();
        	
    		FidoProcess process = new FidoProcess(new_tmp_psm_graph_file.getAbsolutePath(), alpha_value, beta_value, gamma_value);
        	protein2p_fido = process.computeProteinInference();
    	}
    	
    	// 3. output probability for those rows containing protein accesion ID
    	if (!protein2p_fido.isEmpty()) {
    		
    		
    		for (Map.Entry<List<String>, String> proteinIt : protein2p_fido.entrySet()) {
    			
    			StringBuilder sb = new StringBuilder();
    			for (String protein_ID : proteinIt.getKey()) {
    				if (sb.length() > 0) {
    					sb.append(';');
    				}
    				sb.append(proteinLongnames.get(Integer.valueOf(protein_ID)));
    			}
    			
	    		RowKey key = new RowKey(sb.toString());
                DataCell[] cells = new DataCell[2];
                
	    		cells[0] = new StringCell(sb.toString());
	    		cells[1] = new StringCell(proteinIt.getValue());
	    		
	    		DataRow row = new DefaultRow(key, cells);
	    		container.addRowToTable(row);
    		}
    	} else {
    		logger.error("output of Fido is empty...");
    	}
    	
    	new_tmp_psm_graph_file.delete();
    	new_tmp_targetdecoy_file.delete();
    	
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_peptide_column.saveSettingsTo(settings);
        m_accsn_column.saveSettingsTo(settings);
        m_proba_column.saveSettingsTo(settings);
        
        decoy_label_parameter.saveSettingsTo(settings);
        choose_parameters.saveSettingsTo(settings);
        
        alpha_parameter.saveSettingsTo(settings);
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
         
         decoy_label_parameter.loadSettingsFrom(settings);
         choose_parameters.loadSettingsFrom(settings);
         
         alpha_parameter.loadSettingsFrom(settings);
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
    	
    	decoy_label_parameter.validateSettings(settings);
    	choose_parameters.validateSettings(settings);
    	
    	alpha_parameter.validateSettings(settings);
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

