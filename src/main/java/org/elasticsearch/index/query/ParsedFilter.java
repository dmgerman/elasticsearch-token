begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|Filter
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
name|Queries
import|;
end_import

begin_class
DECL|class|ParsedFilter
specifier|public
class|class
name|ParsedFilter
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|ParsedFilter
name|EMPTY
init|=
operator|new
name|ParsedFilter
argument_list|(
name|Queries
operator|.
name|MATCH_NO_FILTER
argument_list|,
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|Filter
operator|>
name|of
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|field|namedFilters
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Filter
argument_list|>
name|namedFilters
decl_stmt|;
DECL|method|ParsedFilter
specifier|public
name|ParsedFilter
parameter_list|(
name|Filter
name|filter
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Filter
argument_list|>
name|namedFilters
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|namedFilters
operator|=
name|namedFilters
expr_stmt|;
block|}
DECL|method|filter
specifier|public
name|Filter
name|filter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
DECL|method|namedFilters
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Filter
argument_list|>
name|namedFilters
parameter_list|()
block|{
return|return
name|namedFilters
return|;
block|}
block|}
end_class

end_unit

