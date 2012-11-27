begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.common.lucene.spatial.prefix
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|spatial
operator|.
name|prefix
package|;
end_package

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Shape
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|jts
operator|.
name|JtsGeometry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|Geometry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|operation
operator|.
name|buffer
operator|.
name|BufferOp
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|operation
operator|.
name|buffer
operator|.
name|BufferParameters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Term
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoShapeConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|ShapeBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|TermFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|XBooleanFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|XTermsFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|spatial
operator|.
name|SpatialStrategy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|spatial
operator|.
name|prefix
operator|.
name|tree
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|spatial
operator|.
name|prefix
operator|.
name|tree
operator|.
name|SpatialPrefixTree
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|FieldMapper
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Implementation of {@link SpatialStrategy} that uses TermQuerys / TermFilters  * to query and filter for Shapes related to other Shapes.  */
end_comment

begin_class
DECL|class|TermQueryPrefixTreeStrategy
specifier|public
class|class
name|TermQueryPrefixTreeStrategy
extends|extends
name|SpatialStrategy
block|{
DECL|field|WITHIN_BUFFER_DISTANCE
specifier|private
specifier|static
specifier|final
name|double
name|WITHIN_BUFFER_DISTANCE
init|=
literal|0.5
decl_stmt|;
DECL|field|BUFFER_PARAMETERS
specifier|private
specifier|static
specifier|final
name|BufferParameters
name|BUFFER_PARAMETERS
init|=
operator|new
name|BufferParameters
argument_list|(
literal|3
argument_list|,
name|BufferParameters
operator|.
name|CAP_SQUARE
argument_list|)
decl_stmt|;
comment|/**      * Creates a new TermQueryPrefixTreeStrategy      *      * @param fieldName        Name of the field the Strategy applies to      * @param prefixTree       SpatialPrefixTree that will be used to represent Shapes      * @param distanceErrorPct Distance Error Percentage used to guide the      *                         SpatialPrefixTree on how precise it should be      */
DECL|method|TermQueryPrefixTreeStrategy
specifier|public
name|TermQueryPrefixTreeStrategy
parameter_list|(
name|FieldMapper
operator|.
name|Names
name|fieldName
parameter_list|,
name|SpatialPrefixTree
name|prefixTree
parameter_list|,
name|double
name|distanceErrorPct
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|prefixTree
argument_list|,
name|distanceErrorPct
argument_list|)
expr_stmt|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|createIntersectsFilter
specifier|public
name|Filter
name|createIntersectsFilter
parameter_list|(
name|Shape
name|shape
parameter_list|)
block|{
name|int
name|detailLevel
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getLevelForDistance
argument_list|(
name|calcDistanceFromErrPct
argument_list|(
name|shape
argument_list|,
name|getDistanceErrorPct
argument_list|()
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|nodes
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getNodes
argument_list|(
name|shape
argument_list|,
name|detailLevel
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Term
index|[]
name|nodeTerms
init|=
operator|new
name|Term
index|[
name|nodes
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nodes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|nodeTerms
index|[
name|i
index|]
operator|=
name|getFieldName
argument_list|()
operator|.
name|createIndexNameTerm
argument_list|(
name|nodes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTokenString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|XTermsFilter
argument_list|(
name|nodeTerms
argument_list|)
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|createIntersectsQuery
specifier|public
name|Query
name|createIntersectsQuery
parameter_list|(
name|Shape
name|shape
parameter_list|)
block|{
name|int
name|detailLevel
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getLevelForDistance
argument_list|(
name|calcDistanceFromErrPct
argument_list|(
name|shape
argument_list|,
name|getDistanceErrorPct
argument_list|()
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|nodes
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getNodes
argument_list|(
name|shape
argument_list|,
name|detailLevel
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|query
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
name|getFieldName
argument_list|()
operator|.
name|createIndexNameTerm
argument_list|(
name|node
operator|.
name|getTokenString
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ConstantScoreQuery
argument_list|(
name|query
argument_list|)
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|createDisjointFilter
specifier|public
name|Filter
name|createDisjointFilter
parameter_list|(
name|Shape
name|shape
parameter_list|)
block|{
name|int
name|detailLevel
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getLevelForDistance
argument_list|(
name|calcDistanceFromErrPct
argument_list|(
name|shape
argument_list|,
name|getDistanceErrorPct
argument_list|()
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|nodes
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getNodes
argument_list|(
name|shape
argument_list|,
name|detailLevel
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|XBooleanFilter
name|filter
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|filter
operator|.
name|add
argument_list|(
operator|new
name|TermFilter
argument_list|(
name|getFieldName
argument_list|()
operator|.
name|createIndexNameTerm
argument_list|(
name|node
operator|.
name|getTokenString
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|createDisjointQuery
specifier|public
name|Query
name|createDisjointQuery
parameter_list|(
name|Shape
name|shape
parameter_list|)
block|{
name|int
name|detailLevel
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getLevelForDistance
argument_list|(
name|calcDistanceFromErrPct
argument_list|(
name|shape
argument_list|,
name|getDistanceErrorPct
argument_list|()
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|nodes
init|=
name|getPrefixTree
argument_list|()
operator|.
name|getNodes
argument_list|(
name|shape
argument_list|,
name|detailLevel
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|query
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
name|getFieldName
argument_list|()
operator|.
name|createIndexNameTerm
argument_list|(
name|node
operator|.
name|getTokenString
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ConstantScoreQuery
argument_list|(
name|query
argument_list|)
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|createWithinFilter
specifier|public
name|Filter
name|createWithinFilter
parameter_list|(
name|Shape
name|shape
parameter_list|)
block|{
name|Filter
name|intersectsFilter
init|=
name|createIntersectsFilter
argument_list|(
name|shape
argument_list|)
decl_stmt|;
name|Geometry
name|shapeGeometry
init|=
name|ShapeBuilder
operator|.
name|toJTSGeometry
argument_list|(
name|shape
argument_list|)
decl_stmt|;
name|Geometry
name|buffer
init|=
name|BufferOp
operator|.
name|bufferOp
argument_list|(
name|shapeGeometry
argument_list|,
name|WITHIN_BUFFER_DISTANCE
argument_list|,
name|BUFFER_PARAMETERS
argument_list|)
decl_stmt|;
name|Shape
name|bufferedShape
init|=
operator|new
name|JtsGeometry
argument_list|(
name|buffer
operator|.
name|difference
argument_list|(
name|shapeGeometry
argument_list|)
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Filter
name|bufferedFilter
init|=
name|createIntersectsFilter
argument_list|(
name|bufferedShape
argument_list|)
decl_stmt|;
name|XBooleanFilter
name|filter
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
name|filter
operator|.
name|add
argument_list|(
name|intersectsFilter
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|filter
operator|.
name|add
argument_list|(
name|bufferedFilter
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
return|return
name|filter
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|createWithinQuery
specifier|public
name|Query
name|createWithinQuery
parameter_list|(
name|Shape
name|shape
parameter_list|)
block|{
name|Query
name|intersectsQuery
init|=
name|createIntersectsQuery
argument_list|(
name|shape
argument_list|)
decl_stmt|;
name|Geometry
name|shapeGeometry
init|=
name|ShapeBuilder
operator|.
name|toJTSGeometry
argument_list|(
name|shape
argument_list|)
decl_stmt|;
name|Geometry
name|buffer
init|=
name|BufferOp
operator|.
name|bufferOp
argument_list|(
name|shapeGeometry
argument_list|,
name|WITHIN_BUFFER_DISTANCE
argument_list|,
name|BUFFER_PARAMETERS
argument_list|)
decl_stmt|;
name|Shape
name|bufferedShape
init|=
operator|new
name|JtsGeometry
argument_list|(
name|buffer
operator|.
name|difference
argument_list|(
name|shapeGeometry
argument_list|)
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Query
name|bufferedQuery
init|=
name|createIntersectsQuery
argument_list|(
name|bufferedShape
argument_list|)
decl_stmt|;
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
name|query
operator|.
name|add
argument_list|(
name|intersectsQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|bufferedQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
return|return
operator|new
name|ConstantScoreQuery
argument_list|(
name|query
argument_list|)
return|;
block|}
block|}
end_class

end_unit

