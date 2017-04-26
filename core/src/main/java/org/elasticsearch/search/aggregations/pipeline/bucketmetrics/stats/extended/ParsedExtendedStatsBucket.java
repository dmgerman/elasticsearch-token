begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.extended
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
name|bucketmetrics
operator|.
name|stats
operator|.
name|extended
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
name|XContentParser
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
name|metrics
operator|.
name|stats
operator|.
name|extended
operator|.
name|ParsedExtendedStats
import|;
end_import

begin_class
DECL|class|ParsedExtendedStatsBucket
specifier|public
class|class
name|ParsedExtendedStatsBucket
extends|extends
name|ParsedExtendedStats
implements|implements
name|ExtendedStatsBucket
block|{
annotation|@
name|Override
DECL|method|getType
specifier|protected
name|String
name|getType
parameter_list|()
block|{
return|return
name|ExtendedStatsBucketPipelineAggregationBuilder
operator|.
name|NAME
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|ParsedExtendedStatsBucket
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedExtendedStatsBucket
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedExtendedStatsBucket
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|declareExtendedStatsFields
argument_list|(
name|PARSER
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedExtendedStatsBucket
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
block|{
name|ParsedExtendedStatsBucket
name|parsedStatsBucket
init|=
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|parsedStatsBucket
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|parsedStatsBucket
return|;
block|}
block|}
end_class

end_unit

