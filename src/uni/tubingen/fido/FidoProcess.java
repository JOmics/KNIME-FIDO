package uni.tubingen.fido;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;


public class FidoProcess {
	
	//fields

    private static volatile FidoProcess instance = null;

    private static String GAMMA_PARAMETER = null;

    private static String BETA_PARAMETER = null;

    private static String ALFA_PARAMETER = null;

    // private PsmFile psm_file;

    // private String file_name = null;

    private static final String FIDO_COMMAND = "./Fido";

    // private static final String DIRECTORY_FIDO_COMPILED = "/Users/enrique/Desktop/command_fido";

    private ProcessBuilder pb = null;



    //singleton instance

    public static FidoProcess getInstance(String psm_file, String alfa, String beta, String gamma) throws Exception {
       if (instance == null) {
         synchronized (FidoProcess.class){
             if (instance == null) {
                 instance = new FidoProcess(psm_file, alfa, beta, gamma);
             }
         }
       }
        return instance;
    }

    //constructor
    private FidoProcess(String psm_file, String alfa, String beta, String gamma) throws Exception {

      // URL url = FidoProcess.class.getClassLoader().getResource("/Users/enrique/Desktop/command_fido");
      // if (url == null) {
      //    throw new IllegalStateException("The Fido program was not found!!");
      // }

        File fido_program = new File("/media/Datos/TRABAJO/KNIME workflow/workspace/FidoProteinInference/src/resources");  //partial solution- no functional in other platform!!!
        
        FidoProcess.ALFA_PARAMETER = alfa;
        FidoProcess.BETA_PARAMETER = beta;
        FidoProcess.GAMMA_PARAMETER = gamma;

        //this.psm_file = psm;

        this.pb = new ProcessBuilder(FIDO_COMMAND, psm_file, ALFA_PARAMETER, BETA_PARAMETER, GAMMA_PARAMETER);
        this.pb.directory(fido_program);
    }


    public HashMap<String, String> computeProteinInference(){

        String s;
        String [] proba_proteins = new String [1];
        HashMap<String, String> proba_protList = new  HashMap<String, String>();
        String regex = " ";

        try {

              Process p = pb.start();

              BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

              BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

              // read the output from the command
              System.out.println("Here is the standard output of the command:\n");

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
              System.out.println(proba_protList);

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
        return FidoProcess.ALFA_PARAMETER = alfa;
    }

    protected String setBetaParameter (String beta){
       return FidoProcess.BETA_PARAMETER = beta;
    }

    protected String setGammaParameter (String gamma){
        return FidoProcess.GAMMA_PARAMETER = gamma;
    }

    public String getAlfaParameter(){
        return FidoProcess.ALFA_PARAMETER;
    }

    public String getGammaParameter(){
        return FidoProcess.GAMMA_PARAMETER;
    }

    public String getBetaParameter(){
        return FidoProcess.BETA_PARAMETER;
    }
    
}
