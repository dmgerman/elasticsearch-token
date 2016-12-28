begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|AbstractDiffable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|Diff
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
name|ParseField
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
name|ParseFieldMatcherSupplier
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
name|bytes
operator|.
name|BytesReference
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
name|ContextParser
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
name|XContentHelper
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Encapsulates a pipeline's id and configuration as a blob  */
end_comment

begin_class
DECL|class|PipelineConfiguration
specifier|public
specifier|final
class|class
name|PipelineConfiguration
extends|extends
name|AbstractDiffable
argument_list|<
name|PipelineConfiguration
argument_list|>
implements|implements
name|ToXContent
block|{
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|Builder
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"pipeline_config"
argument_list|,
name|Builder
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|PARSER
operator|.
name|declareString
argument_list|(
name|Builder
operator|::
name|setId
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|parser
parameter_list|,
name|builder
parameter_list|,
name|aVoid
parameter_list|)
lambda|->
block|{
name|XContentBuilder
name|contentBuilder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|parser
operator|.
name|contentType
argument_list|()
operator|.
name|xContent
argument_list|()
argument_list|)
decl_stmt|;
name|XContentHelper
operator|.
name|copyCurrentStructure
argument_list|(
name|contentBuilder
operator|.
name|generator
argument_list|()
argument_list|,
name|parser
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setConfig
argument_list|(
name|contentBuilder
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"config"
argument_list|)
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
block|}
DECL|method|getParser
specifier|public
specifier|static
name|ContextParser
argument_list|<
name|ParseFieldMatcherSupplier
argument_list|,
name|PipelineConfiguration
argument_list|>
name|getParser
parameter_list|()
block|{
return|return
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|PARSER
operator|.
name|apply
argument_list|(
name|p
argument_list|,
name|c
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|class|Builder
specifier|private
specifier|static
class|class
name|Builder
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|config
specifier|private
name|BytesReference
name|config
decl_stmt|;
DECL|method|setId
name|void
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|setConfig
name|void
name|setConfig
parameter_list|(
name|BytesReference
name|config
parameter_list|)
block|{
name|this
operator|.
name|config
operator|=
name|config
expr_stmt|;
block|}
DECL|method|build
name|PipelineConfiguration
name|build
parameter_list|()
block|{
return|return
operator|new
name|PipelineConfiguration
argument_list|(
name|id
argument_list|,
name|config
argument_list|)
return|;
block|}
block|}
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
comment|// Store config as bytes reference, because the config is only used when the pipeline store reads the cluster state
comment|// and the way the map of maps config is read requires a deep copy (it removes instead of gets entries to check for unused options)
comment|// also the get pipeline api just directly returns this to the caller
DECL|field|config
specifier|private
specifier|final
name|BytesReference
name|config
decl_stmt|;
DECL|method|PipelineConfiguration
specifier|public
name|PipelineConfiguration
parameter_list|(
name|String
name|id
parameter_list|,
name|BytesReference
name|config
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|config
operator|=
name|config
expr_stmt|;
block|}
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|getConfigAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getConfigAsMap
parameter_list|()
block|{
return|return
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|config
argument_list|,
literal|true
argument_list|)
operator|.
name|v2
argument_list|()
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
literal|"id"
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"config"
argument_list|,
name|getConfigAsMap
argument_list|()
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
DECL|method|readFrom
specifier|public
specifier|static
name|PipelineConfiguration
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|PipelineConfiguration
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readBytesReference
argument_list|()
argument_list|)
return|;
block|}
DECL|method|readDiffFrom
specifier|public
specifier|static
name|Diff
argument_list|<
name|PipelineConfiguration
argument_list|>
name|readDiffFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readDiffFrom
argument_list|(
name|PipelineConfiguration
operator|::
name|readFrom
argument_list|,
name|in
argument_list|)
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
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|PipelineConfiguration
name|that
init|=
operator|(
name|PipelineConfiguration
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|id
operator|.
name|equals
argument_list|(
name|that
operator|.
name|id
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
name|config
operator|.
name|equals
argument_list|(
name|that
operator|.
name|config
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|id
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|config
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

