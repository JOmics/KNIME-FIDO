package uni.tubingen.inference.fido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;


public class FidoChooseParametersProcess {
	
    private static final String FIDO_COMMAND = "FidoChooseParameters";
    
    private ProcessBuilder pb;


    public FidoChooseParametersProcess(String psmFile, String targetdecoyFile, boolean useGrouping) {
        this.pb = new ProcessBuilder(FIDO_COMMAND, "-p");
        
        if (useGrouping) {
            pb.command().add("-g");
        }
        
        pb.command().add("-c 1");
        
        pb.command().add(psmFile);
        pb.command().add(targetdecoyFile);
        
        this.pb.directory(null);
    }
    
    
    public HashMap<String, String> computeProteinInference(){
		HashMap<String, String> proba_protList = new  HashMap<String, String>();
		
		try {
			System.out.println("will call:\n\t" + pb.command());
			
			Process p = pb.start();
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			// read the output from the command
			String s;
			while ((s = stdInput.readLine()) != null) {
				if (s.contains(" {")){
					String [] proba_proteins = s.split(" ", 2);
					
					StringTokenizer st = new StringTokenizer(proba_proteins[1], " ");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (!token.equals("{") && !token.equals(",") && !token.equals("}")) {
							proba_protList.put(token, proba_proteins[0]);
						}
					}
				}
			}
			
			System.out.println("Here is the standard error of the command (shows the best params):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
			
		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
		}
		return proba_protList;
    }
}
