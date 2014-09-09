package uni.tubingen.fido;

import org.knime.core.node.NodeView;


/**
 * <code>NodeView</code> for the "FidoProteinInference" Node.
 * 
 *
 * @author enrique
 */
public class FidoProteinInferenceNodeView extends NodeView<FidoProteinInferenceNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link FidoProteinInferenceNodeModel})
     */
    protected FidoProteinInferenceNodeView(final FidoProteinInferenceNodeModel nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    	FidoProteinInferenceNodeModel nodeModel = 
                getNodeModel();
            assert nodeModel != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

