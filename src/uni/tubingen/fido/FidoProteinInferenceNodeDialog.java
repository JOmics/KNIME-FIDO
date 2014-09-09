package uni.tubingen.fido;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;





/**
 * <code>NodeDialog</code> for the "FidoProteinInference" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author enrique
 */
public class FidoProteinInferenceNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the FidoProteinInference node.
     */
  
	@SuppressWarnings("unchecked")
	protected FidoProteinInferenceNodeDialog() {
              super();
        
            final SettingsModelString matches = new SettingsModelString(FidoProteinInferenceNodeModel.CFGKEY_PEPTIDES, "Peptides");
          	final SettingsModelString accsn   = new SettingsModelString(FidoProteinInferenceNodeModel.CFGKEY_PROTEIN, "Protein");
          	final SettingsModelString probabilities  = new SettingsModelString(FidoProteinInferenceNodeModel.CFGKEY_PROBA, "Probabilities");
          
          	final SettingsModelDouble alfa      = new SettingsModelDouble(FidoProteinInferenceNodeModel.CFGKEY_ALFA_PARAMETER, 0.5);
          	final SettingsModelDouble beta      = new SettingsModelDouble(FidoProteinInferenceNodeModel.CFGKEY_BETA_PARAMETER, 0.1);
          	final SettingsModelDouble gamma      = new SettingsModelDouble(FidoProteinInferenceNodeModel.CFGKEY_GAMMA_PARAMETER, 0.05);
          	
   
              addDialogComponent(new DialogComponentColumnNameSelection(accsn, "Accession Column", 0, true, StringValue.class));
              addDialogComponent(new DialogComponentColumnNameSelection(matches, "Peptides Column", 0, true, 
            		             new ColumnFilter() {

      		                	@Override
      							public boolean includeColumn(DataColumnSpec colSpec) {
      								if (colSpec.getType().isCollectionType() && colSpec.getType().getCollectionElementType().isCompatible(StringValue.class))
      									return true;
      								
      								if (colSpec.getType().isCompatible(StringValue.class)) 
      									return true;
      								
      								return false;
      							}

      							@Override
      							public String allFilteredMsg() {
      								return "No suitable columns (string or List/Set column) to select!";
      							}
              			
              		}));
               addDialogComponent(new DialogComponentColumnNameSelection(probabilities, "Probabilities", 0, true, StringValue.class));
              
               addDialogComponent(new DialogComponentNumber(new SettingsModelDoubleBounded(FidoProteinInferenceNodeModel.CFGKEY_ALFA_PARAMETER, 0.5, 0.05, 1.0),  "Alfa",  0.5));
               addDialogComponent(new DialogComponentNumber(new SettingsModelDoubleBounded(FidoProteinInferenceNodeModel.CFGKEY_BETA_PARAMETER, 0.1, 0.05, 1.0),  "Beta",  0.1));
               addDialogComponent(new DialogComponentNumber(new SettingsModelDoubleBounded(FidoProteinInferenceNodeModel.CFGKEY_GAMMA_PARAMETER, 0.05, 0.01, 1.0),  "Gamma",  0.05));

               //addDialogComponent(new DialogComponentStringSelection(new SettingsModelString(FidoProteinInferenceNodeModel.CFGKEY_ALGO, items[0]), "Algorithm", items));
               
             }
}

