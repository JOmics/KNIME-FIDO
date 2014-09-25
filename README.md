Knime-fido
===============

# About KNIME-FidoProteinInference Node

FidoProteinInference is a KNIME node implementation to perform protein inference analysis on Mass Spectrometry data. Fido is a probabilistic model for protein identification in tandem mass spectrometry that recognizes peptide degeneracy.

# License

Kinme-Fido is a JOmics library licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

# How to cite it:

Serang, O., MacCoss, M.J., Noble, W.S., 2010. Efficient marginalization to compute protein posterior probabilities from shotgun mass spectrometry data. Journal of Proteome Research 9 (10), 5346–5357.

https://noble.gs.washington.edu/proj/fido/

# Main Features

* Fast and efficient processing of large dataset (tabular format like input).

* Simple structure (one port input/one port output).

* Requires peptide's Posterior Error Probabilities (PEP). It could be computed with PeptideProphet or comparable.

* Compute posterior error probabilities for proteins.

* Possibility of easy integration with OpenMS workflow (into KNIME).

 
# Node Input/Output

Basically, the FidoProteinInference node handle the data in tabular format. Input: Peptide list with proteins group and peptide probability linked. Output: report Protein list with probability computed.

**Note**: The node is still evolving, we are committed to expand the node and add more features such as additional settings in the configuration dialog to process optimization.


# Getting ProteinLassoInference node

## Installation Requirements


* Java: KNIME SDK 9.2 (or above) and Java JRE 1.6 (or above), which you can download for free here. (Note: most computers should have Java installed already).

* Operating System: The current version has been tested on Linux and Max OS X, it may require additional adjustment for other platform. If you come across any problems on your platform, please contact the ??????.

* Memory: MS dataset can be very large sometimes, in order to get good performance from this KNIME node, we recommend the following settings for VM arguments: -ea -Xmx1G -XX:MaxPermSize=512M. For additional information see https://tech.knime.org/test-your-node .


## Launch via Eclipse project

Click (here)
(https://github.com/JOmics/KNIME-FIDO ) to launch directly the latest FidoProteinInference (KNIME node) implementation.

This node is ready to use. However, taking into account you current platform, it may requiere additional adjustment. You can build the standalone node in few step (see https://tech.knime.org/developer/documentation/export).  

You can get the latest FidoProteinInference node implementation from our Download Section. Unzipping the file and load via Eclipse project.


## Download

You can get the latest FidoProteinInference node implementation from our Download Section. Unzipping the file and load via Eclipse project

##Maven Dependency


# Getting Help

If you have questions or need additional help, please contact us via Enrique Audain Martinez (enriquea@cim.sld.cu) and Yasset Perez-Riverol ?(yperez@ebi.ac.uk).

Please send us your feedback, including error reports, improvement suggestions, new feature requests and any other things you might want to suggest.


# Screenshots

Peptide/Protein Identification workflow (OpenMS-KNIME). (1) Mass spectrometry data pre-processing. (2) Database searching/Peptide identification. (3) idXML file processing. (4) Peptide detectability prediction based on SVM approach. (5) Protein inference analysis.

 
