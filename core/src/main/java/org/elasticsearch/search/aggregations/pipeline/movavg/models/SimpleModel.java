begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.movavg.models
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
operator|.
name|movavg
operator|.
name|models
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
name|Nullable
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
name|pipeline
operator|.
name|movavg
operator|.
name|MovAvgParser
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
name|text
operator|.
name|ParseException
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
name|Collection
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
comment|/**  * Calculate a simple unweighted (arithmetic) moving average  */
end_comment

begin_class
DECL|class|SimpleModel
specifier|public
class|class
name|SimpleModel
extends|extends
name|MovAvgModel
block|{
DECL|field|NAME_FIELD
specifier|protected
specifier|static
specifier|final
name|ParseField
name|NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"simple"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|doPredict
specifier|protected
parameter_list|<
name|T
extends|extends
name|Number
parameter_list|>
name|double
index|[]
name|doPredict
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|values
parameter_list|,
name|int
name|numPredictions
parameter_list|)
block|{
name|double
index|[]
name|predictions
init|=
operator|new
name|double
index|[
name|numPredictions
index|]
decl_stmt|;
comment|// EWMA just emits the same final prediction repeatedly.
name|Arrays
operator|.
name|fill
argument_list|(
name|predictions
argument_list|,
name|next
argument_list|(
name|values
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|predictions
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
parameter_list|<
name|T
extends|extends
name|Number
parameter_list|>
name|double
name|next
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|values
parameter_list|)
block|{
name|double
name|avg
init|=
literal|0
decl_stmt|;
for|for
control|(
name|T
name|v
range|:
name|values
control|)
block|{
name|avg
operator|+=
name|v
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
return|return
name|avg
operator|/
name|values
operator|.
name|size
argument_list|()
return|;
block|}
DECL|field|STREAM
specifier|public
specifier|static
specifier|final
name|MovAvgModelStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|MovAvgModelStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|MovAvgModel
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SimpleModel
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
return|;
block|}
block|}
decl_stmt|;
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
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|SimpleModelParser
specifier|public
specifier|static
class|class
name|SimpleModelParser
extends|extends
name|AbstractModelParser
block|{
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|MovAvgModel
name|parse
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
parameter_list|,
name|String
name|pipelineName
parameter_list|,
name|int
name|windowSize
parameter_list|)
throws|throws
name|ParseException
block|{
return|return
operator|new
name|SimpleModel
argument_list|()
return|;
block|}
block|}
DECL|class|SimpleModelBuilder
specifier|public
specifier|static
class|class
name|SimpleModelBuilder
implements|implements
name|MovAvgModelBuilder
block|{
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
name|field
argument_list|(
name|MovAvgParser
operator|.
name|MODEL
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
block|}
end_class

end_unit

