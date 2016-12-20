begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.nested
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|nested
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParsingException
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentParser
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
name|ObjectMapper
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
name|query
operator|.
name|QueryParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AbstractAggregationBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationExecutionException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorFactories
operator|.
name|Builder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|InternalAggregation
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_class
DECL|class|NestedAggregationBuilder
specifier|public
class|class
name|NestedAggregationBuilder
extends|extends
name|AbstractAggregationBuilder
argument_list|<
name|NestedAggregationBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"nested"
decl_stmt|;
DECL|field|TYPE
specifier|private
specifier|static
specifier|final
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
comment|/**      * @param name      *            the name of this aggregation      * @param path      *            the path to use for this nested aggregation. The path must      *            match the path to a nested object in the mappings.      */
DECL|method|NestedAggregationBuilder
specifier|public
name|NestedAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[path] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|NestedAggregationBuilder
specifier|public
name|NestedAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|TYPE
argument_list|)
expr_stmt|;
name|path
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
comment|/**      * Get the path to use for this nested aggregation.      */
DECL|method|path
specifier|public
name|String
name|path
parameter_list|()
block|{
return|return
name|path
return|;
block|}
annotation|@
name|Override
DECL|method|doBuild
specifier|protected
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|doBuild
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
name|ObjectMapper
name|childObjectMapper
init|=
name|context
operator|.
name|getObjectMapper
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|childObjectMapper
operator|==
literal|null
condition|)
block|{
comment|// in case the path has been unmapped:
return|return
operator|new
name|NestedAggregatorFactory
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
if|if
condition|(
name|childObjectMapper
operator|.
name|nested
argument_list|()
operator|.
name|isNested
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"[nested] nested path ["
operator|+
name|path
operator|+
literal|"] is not nested"
argument_list|)
throw|;
block|}
try|try
block|{
name|ObjectMapper
name|parentObjectMapper
init|=
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|nestedScope
argument_list|()
operator|.
name|nextLevel
argument_list|(
name|childObjectMapper
argument_list|)
decl_stmt|;
return|return
operator|new
name|NestedAggregatorFactory
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|parentObjectMapper
argument_list|,
name|childObjectMapper
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
finally|finally
block|{
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|nestedScope
argument_list|()
operator|.
name|previousLevel
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|NestedAggregator
operator|.
name|PATH_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|NestedAggregationBuilder
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|path
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|NestedAggregator
operator|.
name|PATH_FIELD
argument_list|)
condition|)
block|{
name|path
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
comment|// "field" doesn't exist, so we fall back to the context of the ancestors
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Missing [path] field for nested aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|NestedAggregationBuilder
argument_list|(
name|aggregationName
argument_list|,
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|NestedAggregationBuilder
name|other
init|=
operator|(
name|NestedAggregationBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|path
argument_list|,
name|other
operator|.
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

