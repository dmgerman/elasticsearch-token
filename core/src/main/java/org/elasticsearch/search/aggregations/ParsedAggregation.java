begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
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
name|xcontent
operator|.
name|ObjectParser
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
name|ToXContent
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
operator|.
name|Token
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link Aggregation} that is parsed from a REST response.  * Serves as a base class for all aggregation implementations that are parsed from REST.  */
end_comment

begin_class
DECL|class|ParsedAggregation
specifier|public
specifier|abstract
class|class
name|ParsedAggregation
implements|implements
name|Aggregation
implements|,
name|ToXContent
block|{
DECL|method|declareAggregationFields
specifier|protected
specifier|static
name|void
name|declareAggregationFields
parameter_list|(
name|ObjectParser
argument_list|<
name|?
extends|extends
name|ParsedAggregation
argument_list|,
name|Void
argument_list|>
name|objectParser
parameter_list|)
block|{
name|objectParser
operator|.
name|declareObject
argument_list|(
parameter_list|(
name|parsedAgg
parameter_list|,
name|metadata
parameter_list|)
lambda|->
name|parsedAgg
operator|.
name|metadata
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|metadata
argument_list|)
argument_list|,
parameter_list|(
name|parser
parameter_list|,
name|context
parameter_list|)
lambda|->
name|parser
operator|.
name|map
argument_list|()
argument_list|,
name|InternalAggregation
operator|.
name|CommonFields
operator|.
name|META
argument_list|)
expr_stmt|;
block|}
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|metadata
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metadata
decl_stmt|;
annotation|@
name|Override
DECL|method|getName
specifier|public
specifier|final
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|setName
specifier|protected
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getMetaData
specifier|public
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getMetaData
parameter_list|()
block|{
return|return
name|metadata
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Concatenates the type and the name of the aggregation (ex: top_hits#foo)
name|builder
operator|.
name|startObject
argument_list|(
name|String
operator|.
name|join
argument_list|(
name|InternalAggregation
operator|.
name|TYPED_KEYS_DELIMITER
argument_list|,
name|getType
argument_list|()
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|metadata
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|InternalAggregation
operator|.
name|CommonFields
operator|.
name|META
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|this
operator|.
name|metadata
argument_list|)
expr_stmt|;
block|}
name|doXContentBody
argument_list|(
name|builder
argument_list|,
name|params
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
DECL|method|doXContentBody
specifier|protected
specifier|abstract
name|XContentBuilder
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Parse a token of type XContentParser.Token.VALUE_NUMBER or XContentParser.Token.STRING to a double.      * In other cases the default value is returned instead.      */
DECL|method|parseDouble
specifier|protected
specifier|static
name|double
name|parseDouble
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|double
name|defaultNullValue
parameter_list|)
throws|throws
name|IOException
block|{
name|Token
name|currentToken
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentToken
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
operator|||
name|currentToken
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|parser
operator|.
name|doubleValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|defaultNullValue
return|;
block|}
block|}
block|}
end_class

end_unit

