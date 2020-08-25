/**
 * Specific version of GeneLegend for tested negative genes. It is not displayed but used
 * because of the way gene update machinery works (to update gene IDs for gene known only by their symbols)
 *
 * @class RejectedGeneLegend
 * @constructor
 */
 define(["pedigree/view/geneLegend"], function(GeneLegend){
    var RejectedGeneLegend = Class.create( GeneLegend, {

        initialize: function($super) {
            $super('Tested Negative Genes', 'genes',
                   "rejected",
                   [], // these are never displayed in a legend so don't need colours
                   "getRejectedGenes",
                   "setRejectedGenes", true); // operation
        },

        addCase: function($super, id, symbol, nodeID) {
            $super(id, symbol, nodeID, true);
        }
    });
    return RejectedGeneLegend;
});
