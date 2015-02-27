package uni.tubingen.inference.fido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;


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
				Matcher resultMatcher = FidoProteinInferenceNodeModel.resultPattern.matcher(s);
				if (resultMatcher.matches()) {
					List<String> proteinAccessions = new ArrayList<String>();
					proba_protList.put(proteinAccessions, resultMatcher.group(1));
					
					for (String proteinID : resultMatcher.group(2).split(",")) {
						proteinAccessions.add(proteinID.trim());
					}
				}
			}
			
			StringBuilder outSb = new StringBuilder();
			while ((s = stdError.readLine()) != null) {
				outSb.append(s);
				outSb.append("\n");
			}
			if (outSb.length() > 0) {
				FidoProteinInferenceNodeModel.logger.warn("Fido prints best parameter info to STDERR, so this might be useful or an error:\n"
						+ outSb.toString());
			}
		} catch (IOException e) {
			FidoProteinInferenceNodeModel.logger.error(e);
		}
		return proba_protList;
    }
}
