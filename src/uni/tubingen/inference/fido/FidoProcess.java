package uni.tubingen.inference.fido;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;


public class FidoProcess {
	
	private String gammaParameter = null;
	
	private String alphaParameter = null;
	
	private String betaParameter = null;
	
	private String psmFile = null;
	
	
	/**
	 * constructor
	 * 
	 * @param psm_file
	 * @param alpha
	 * @param beta
	 * @param gamma
	 * @throws Exception
	 */
	public FidoProcess(String psm_file, String alpha, String beta, String gamma) throws Exception {
		this.alphaParameter = alpha;
		this.betaParameter = beta;
		this.gammaParameter = gamma;
		
		this.psmFile = psm_file;
	}
	
	
	public HashMap<List<String>, String> computeProteinInference(){
		HashMap<List<String>, String> proba_protList = new  HashMap<List<String>, String>();
		//String regex = " ";
		
		try {
			String fidoPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			if (!fidoPath.endsWith(File.separator)) {
				// we are in the jar, only get the path to it
				fidoPath = fidoPath.substring(0, fidoPath.lastIndexOf(File.separator) + 1);
			}
			fidoPath += "executables" + File.separator + "Fido";
			
			ProcessBuilder pb = new ProcessBuilder(fidoPath, psmFile, gammaParameter, alphaParameter, betaParameter);
			pb.directory(null);
			
			System.out.println("will call:\n\t" + pb.command());
			
			Process p = pb.start();
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			// read the output from the command
			String s;
			while ((s = stdInput.readLine()) != null) {
				Matcher resultMatcher = FidoProteinInferenceNodeModel.resultPattern.matcher(s);
				if (resultMatcher.matches()) {
					List<String> proteinAccessions = new ArrayList<String>();
					proba_protList.put(proteinAccessions, resultMatcher.group(1));
					
					for (String proteinID : resultMatcher.group(2).split(",")) {
						proteinAccessions.add(proteinID.trim());
					}
				}
			}
			
			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				FidoProteinInferenceNodeModel.logger.warn("Fido puts info to STDERR, this might be an error:\n"
						+ s);
			}
		} catch (Exception e) {
			FidoProteinInferenceNodeModel.logger.error(e);
		}
		
		return proba_protList;
	}
	
	
    protected String setAlfaParameter (String alfa){
        return alphaParameter = alfa;
    }

    protected String setBetaParameter (String beta){
       return betaParameter = beta;
    }

    protected String setGammaParameter (String gamma){
        return gammaParameter = gamma;
    }

    public String getAlfaParameter(){
        return alphaParameter;
    }

    public String getGammaParameter(){
        return gammaParameter;
    }

    public String getBetaParameter(){
        return betaParameter;
    }
    
}
