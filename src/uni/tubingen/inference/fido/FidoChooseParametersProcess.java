package uni.tubingen.inference.fido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    
    public HashMap<List<String>, String> computeProteinInference(){
    	HashMap<List<String>, String> proba_protList = new  HashMap<List<String>, String>();
		
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
					
					List<String> proteinAccessions = new ArrayList<String>();
					proba_protList.put(proteinAccessions, proba_proteins[0]);
					
					StringTokenizer st = new StringTokenizer(proba_proteins[1], " ");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (!token.equals("{") && !token.equals(",") && !token.equals("}")) {
							proteinAccessions.add(token);
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
