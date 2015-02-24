package uni.tubingen.inference.fido;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;


public class FidoProcess {
	
	//fields
    private String gammaParameter = null;

    private String betaParameter = null;

    private String alphaParameter = null;

    // private PsmFile psm_file;

    // private String file_name = null;

    private static final String FIDO_COMMAND = "Fido";
    
    // private static final String DIRECTORY_FIDO_COMPILED = "/Users/enrique/Desktop/command_fido";

    private ProcessBuilder pb = null;


    //constructor
    public FidoProcess(String psm_file, String alpha, String beta, String gamma) throws Exception {
        //File fido_program = new File("/media/Datos/TRABAJO/KNIME workflow/workspace/FidoProteinInference/src/resources");
        
        alphaParameter = alpha;
        betaParameter = beta;
        gammaParameter = gamma;
        
        //this.psm_file = psm;
        
        this.pb = new ProcessBuilder(FIDO_COMMAND, psm_file, gammaParameter, alphaParameter, betaParameter);
        this.pb.directory(null);
    }


    public HashMap<String, String> computeProteinInference(){

        String s;
        String [] proba_proteins = new String [1];
        HashMap<String, String> proba_protList = new  HashMap<String, String>();
        String regex = " ";

        try {
        	System.out.println("will call:\n\t" + pb.command());

              Process p = pb.start();

              BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

              BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

              // read the output from the command
              while ((s = stdInput.readLine()) != null && s.contains(regex)){
            	 proba_proteins = s.split(regex, 2);
            	  StringTokenizer st = new StringTokenizer(proba_proteins[1]);
            	     while (st.hasMoreTokens()) {
            	    	 String token = st.nextToken(); //the protein ID string can't the follow patterns: " { } , " ...in this case it will be rejected
            	    	   if (!token.contains("{") && !token.contains(",") && !token.contains("}")){ //token is protein ID?
            	    		   proba_protList.put(token, proba_proteins[0]);
            	    	   }       
            	     }          
                }
              //System.out.println(proba_protList);

            // read any errors from the attempted command
             System.out.println("Here is the standard error of the command (if any):\n");
              while ((s = stdError.readLine()) != null) {
                  System.out.println(s);
              }

            //  System.exit(0);
        }

        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
             e.printStackTrace();
           // System.exit(-1);
        }
        
        return proba_protList;
    }



    protected void setCurrentFidoPath(String fido_path){
        pb.directory(new File(fido_path));
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
