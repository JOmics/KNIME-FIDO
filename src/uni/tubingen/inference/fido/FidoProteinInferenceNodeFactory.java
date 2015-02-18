package uni.tubingen.inference.fido;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FidoProteinInference" Node.
 * 
 *
 * @author enrique
 */
public class FidoProteinInferenceNodeFactory 
        extends NodeFactory<FidoProteinInferenceNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FidoProteinInferenceNodeModel createNodeModel() {
        return new FidoProteinInferenceNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<FidoProteinInferenceNodeModel> createNodeView(final int viewIndex,
            final FidoProteinInferenceNodeModel nodeModel) {
        return new FidoProteinInferenceNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new FidoProteinInferenceNodeDialog();
    }

}

