package uni.tubingen.inference.fido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


public class FidoProcess {
	
	private String gammaParameter = null;
	
	private String alphaParameter = null;
	
	private String betaParameter = null;
	
	private String psmFile = null;
	
	private static final String FIDO_COMMAND = "Fido";
	
	
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
		String s;
		String [] proba_proteins = new String [1];
		HashMap<List<String>, String> proba_protList = new  HashMap<List<String>, String>();
		String regex = " ";
		
		try {
			ProcessBuilder pb = new ProcessBuilder(FIDO_COMMAND, psmFile, gammaParameter, alphaParameter, betaParameter);
			pb.directory(null);
			
			System.out.println("will call:\n\t" + pb.command());
			
			Process p = pb.start();
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			// read the output from the command
			while ((s = stdInput.readLine()) != null && s.contains(regex)){
				proba_proteins = s.split(regex, 2);
				
				List<String> proteinAccessions = new ArrayList<String>();
				proba_protList.put(proteinAccessions, proba_proteins[0]);
				
				StringTokenizer st = new StringTokenizer(proba_proteins[1]);
				while (st.hasMoreTokens()) {
					String token = st.nextToken(); //the protein ID string can't the follow patterns: " { } , " ...in this case it will be rejected
					if (!token.contains("{") && !token.contains(",") && !token.contains("}")) {
						//token is protein ID
						proteinAccessions.add(token);
					}
				}
			}
			
			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
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
