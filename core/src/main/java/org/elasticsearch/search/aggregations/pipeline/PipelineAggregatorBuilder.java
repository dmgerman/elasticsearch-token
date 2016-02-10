begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|pipeline
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|ToXContentToBytes
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
name|NamedWriteable
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
name|search
operator|.
name|aggregations
operator|.
name|AggregatorFactory
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
name|Arrays
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_comment
comment|/**  * A factory that knows how to create an {@link PipelineAggregator} of a  * specific type.  */
end_comment

begin_class
DECL|class|PipelineAggregatorBuilder
specifier|public
specifier|abstract
class|class
name|PipelineAggregatorBuilder
extends|extends
name|ToXContentToBytes
implements|implements
name|NamedWriteable
argument_list|<
name|PipelineAggregatorBuilder
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|name
specifier|protected
name|String
name|name
decl_stmt|;
DECL|field|type
specifier|protected
name|String
name|type
decl_stmt|;
DECL|field|bucketsPaths
specifier|protected
name|String
index|[]
name|bucketsPaths
decl_stmt|;
DECL|field|metaData
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
decl_stmt|;
comment|/**      * Constructs a new pipeline aggregator factory.      *      * @param name      *            The aggregation name      * @param type      *            The aggregation type      */
DECL|method|PipelineAggregatorBuilder
specifier|public
name|PipelineAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|)
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[name] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[type] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|bucketsPaths
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[bucketsPaths] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|bucketsPaths
operator|=
name|bucketsPaths
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Validates the state of this factory (makes sure the factory is properly      * configured)      */
DECL|method|validate
specifier|public
specifier|final
name|void
name|validate
parameter_list|(
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
index|[]
name|factories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregatorBuilder
argument_list|>
name|pipelineAggregatorFactories
parameter_list|)
block|{
name|doValidate
argument_list|(
name|parent
argument_list|,
name|factories
argument_list|,
name|pipelineAggregatorFactories
argument_list|)
expr_stmt|;
block|}
DECL|method|createInternal
specifier|protected
specifier|abstract
name|PipelineAggregator
name|createInternal
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Creates the pipeline aggregator      *      * @return The created aggregator      */
DECL|method|create
specifier|public
specifier|final
name|PipelineAggregator
name|create
parameter_list|()
throws|throws
name|IOException
block|{
name|PipelineAggregator
name|aggregator
init|=
name|createInternal
argument_list|(
name|this
operator|.
name|metaData
argument_list|)
decl_stmt|;
return|return
name|aggregator
return|;
block|}
DECL|method|doValidate
specifier|public
name|void
name|doValidate
parameter_list|(
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
index|[]
name|factories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregatorBuilder
argument_list|>
name|pipelineAggregatorFactories
parameter_list|)
block|{     }
DECL|method|setMetaData
specifier|public
name|void
name|setMetaData
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getBucketsPaths
specifier|public
name|String
index|[]
name|getBucketsPaths
parameter_list|()
block|{
return|return
name|bucketsPaths
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
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
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|bucketsPaths
argument_list|)
expr_stmt|;
name|doWriteTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeMap
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
block|}
DECL|method|doWriteTo
specifier|protected
specifier|abstract
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|PipelineAggregatorBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|name
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|String
index|[]
name|bucketsPaths
init|=
name|in
operator|.
name|readStringArray
argument_list|()
decl_stmt|;
name|PipelineAggregatorBuilder
name|factory
init|=
name|doReadFrom
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|,
name|in
argument_list|)
decl_stmt|;
name|factory
operator|.
name|metaData
operator|=
name|in
operator|.
name|readMap
argument_list|()
expr_stmt|;
return|return
name|factory
return|;
block|}
DECL|method|doReadFrom
specifier|protected
specifier|abstract
name|PipelineAggregatorBuilder
name|doReadFrom
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|toXContent
specifier|public
specifier|final
name|XContentBuilder
name|toXContent
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
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|metaData
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"meta"
argument_list|,
name|this
operator|.
name|metaData
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|overrideBucketsPath
argument_list|()
operator|&&
name|bucketsPaths
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|path
range|:
name|bucketsPaths
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|internalXContent
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
operator|.
name|endObject
argument_list|()
return|;
block|}
comment|/**      * @return<code>true</code> if the {@link PipelineAggregatorBuilder}      *         overrides the XContent rendering of the bucketPath option.      */
DECL|method|overrideBucketsPath
specifier|protected
name|boolean
name|overrideBucketsPath
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|internalXContent
specifier|protected
specifier|abstract
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
function_decl|;
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|Arrays
operator|.
name|hashCode
argument_list|(
name|bucketsPaths
argument_list|)
argument_list|,
name|metaData
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|doHashCode
argument_list|()
argument_list|)
return|;
block|}
DECL|method|doHashCode
specifier|protected
specifier|abstract
name|int
name|doHashCode
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|PipelineAggregatorBuilder
name|other
init|=
operator|(
name|PipelineAggregatorBuilder
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|name
argument_list|,
name|other
operator|.
name|name
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|type
argument_list|,
name|other
operator|.
name|type
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|deepEquals
argument_list|(
name|bucketsPaths
argument_list|,
name|other
operator|.
name|bucketsPaths
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|metaData
argument_list|,
name|other
operator|.
name|metaData
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
name|doEquals
argument_list|(
name|obj
argument_list|)
return|;
block|}
DECL|method|doEquals
specifier|protected
specifier|abstract
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
function_decl|;
block|}
end_class

end_unit

