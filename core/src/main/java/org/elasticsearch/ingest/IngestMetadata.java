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
name|Diff
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
name|Diffable
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
name|DiffableUtils
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
name|metadata
operator|.
name|MetaData
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
name|ArrayList
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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

begin_comment
comment|/**  * Holds the ingest pipelines that are available in the cluster  */
end_comment

begin_class
DECL|class|IngestMetadata
specifier|public
specifier|final
class|class
name|IngestMetadata
implements|implements
name|MetaData
operator|.
name|Custom
block|{
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|String
name|TYPE
init|=
literal|"ingest"
decl_stmt|;
DECL|field|PROTO
specifier|public
specifier|final
specifier|static
name|IngestMetadata
name|PROTO
init|=
operator|new
name|IngestMetadata
argument_list|()
decl_stmt|;
DECL|field|PIPELINES_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|PIPELINES_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"pipeline"
argument_list|)
decl_stmt|;
DECL|field|INGEST_METADATA_PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|List
argument_list|<
name|PipelineConfiguration
argument_list|>
argument_list|,
name|Void
argument_list|>
name|INGEST_METADATA_PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"ingest_metadata"
argument_list|,
name|ArrayList
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|INGEST_METADATA_PARSER
operator|.
name|declareObjectArray
argument_list|(
name|List
operator|::
name|addAll
argument_list|,
name|PipelineConfiguration
operator|.
name|getParser
argument_list|()
argument_list|,
name|PIPELINES_FIELD
argument_list|)
expr_stmt|;
block|}
comment|// We can't use Pipeline class directly in cluster state, because we don't have the processor factories around when
comment|// IngestMetadata is registered as custom metadata.
DECL|field|pipelines
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelines
decl_stmt|;
DECL|method|IngestMetadata
specifier|private
name|IngestMetadata
parameter_list|()
block|{
name|this
operator|.
name|pipelines
operator|=
name|Collections
operator|.
name|emptyMap
argument_list|()
expr_stmt|;
block|}
DECL|method|IngestMetadata
specifier|public
name|IngestMetadata
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelines
parameter_list|)
block|{
name|this
operator|.
name|pipelines
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|pipelines
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|getPipelines
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|getPipelines
parameter_list|()
block|{
return|return
name|pipelines
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|IngestMetadata
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelines
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|PipelineConfiguration
name|pipeline
init|=
name|PipelineConfiguration
operator|.
name|readPipelineConfiguration
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|pipelines
operator|.
name|put
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|pipeline
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|IngestMetadata
argument_list|(
name|pipelines
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
name|writeVInt
argument_list|(
name|pipelines
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|PipelineConfiguration
name|pipeline
range|:
name|pipelines
operator|.
name|values
argument_list|()
control|)
block|{
name|pipeline
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|IngestMetadata
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelines
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PipelineConfiguration
argument_list|>
name|configs
init|=
name|INGEST_METADATA_PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
decl_stmt|;
for|for
control|(
name|PipelineConfiguration
name|pipeline
range|:
name|configs
control|)
block|{
name|pipelines
operator|.
name|put
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|pipeline
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|IngestMetadata
argument_list|(
name|pipelines
argument_list|)
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
name|startArray
argument_list|(
name|PIPELINES_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|PipelineConfiguration
name|pipeline
range|:
name|pipelines
operator|.
name|values
argument_list|()
control|)
block|{
name|pipeline
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|context
specifier|public
name|EnumSet
argument_list|<
name|MetaData
operator|.
name|XContentContext
argument_list|>
name|context
parameter_list|()
block|{
return|return
name|MetaData
operator|.
name|API_AND_GATEWAY
return|;
block|}
annotation|@
name|Override
DECL|method|diff
specifier|public
name|Diff
argument_list|<
name|MetaData
operator|.
name|Custom
argument_list|>
name|diff
parameter_list|(
name|MetaData
operator|.
name|Custom
name|before
parameter_list|)
block|{
return|return
operator|new
name|IngestMetadataDiff
argument_list|(
operator|(
name|IngestMetadata
operator|)
name|before
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readDiffFrom
specifier|public
name|Diff
argument_list|<
name|MetaData
operator|.
name|Custom
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
operator|new
name|IngestMetadataDiff
argument_list|(
name|in
argument_list|)
return|;
block|}
DECL|class|IngestMetadataDiff
specifier|static
class|class
name|IngestMetadataDiff
implements|implements
name|Diff
argument_list|<
name|MetaData
operator|.
name|Custom
argument_list|>
block|{
DECL|field|pipelines
specifier|final
name|Diff
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
argument_list|>
name|pipelines
decl_stmt|;
DECL|method|IngestMetadataDiff
name|IngestMetadataDiff
parameter_list|(
name|IngestMetadata
name|before
parameter_list|,
name|IngestMetadata
name|after
parameter_list|)
block|{
name|this
operator|.
name|pipelines
operator|=
name|DiffableUtils
operator|.
name|diff
argument_list|(
name|before
operator|.
name|pipelines
argument_list|,
name|after
operator|.
name|pipelines
argument_list|,
name|DiffableUtils
operator|.
name|getStringKeySerializer
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|IngestMetadataDiff
specifier|public
name|IngestMetadataDiff
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|pipelines
operator|=
name|DiffableUtils
operator|.
name|readJdkMapDiff
argument_list|(
name|in
argument_list|,
name|DiffableUtils
operator|.
name|getStringKeySerializer
argument_list|()
argument_list|,
name|PipelineConfiguration
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
name|MetaData
operator|.
name|Custom
name|apply
parameter_list|(
name|MetaData
operator|.
name|Custom
name|part
parameter_list|)
block|{
return|return
operator|new
name|IngestMetadata
argument_list|(
name|pipelines
operator|.
name|apply
argument_list|(
operator|(
operator|(
name|IngestMetadata
operator|)
name|part
operator|)
operator|.
name|pipelines
argument_list|)
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
name|pipelines
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
name|IngestMetadata
name|that
init|=
operator|(
name|IngestMetadata
operator|)
name|o
decl_stmt|;
return|return
name|pipelines
operator|.
name|equals
argument_list|(
name|that
operator|.
name|pipelines
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
return|return
name|pipelines
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

