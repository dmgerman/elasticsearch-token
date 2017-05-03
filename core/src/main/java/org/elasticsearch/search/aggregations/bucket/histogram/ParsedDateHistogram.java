begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
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
name|histogram
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
name|DocValueFormat
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
name|ParsedMultiBucketAggregation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|ParsedDateHistogram
specifier|public
class|class
name|ParsedDateHistogram
extends|extends
name|ParsedMultiBucketAggregation
implements|implements
name|Histogram
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
name|DateHistogramAggregationBuilder
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|getBuckets
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|Histogram
operator|.
name|Bucket
argument_list|>
name|getBuckets
parameter_list|()
block|{
return|return
name|buckets
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|bucket
lambda|->
operator|(
name|Histogram
operator|.
name|Bucket
operator|)
name|bucket
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
name|ObjectParser
argument_list|<
name|ParsedDateHistogram
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedDateHistogram
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedDateHistogram
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|declareMultiBucketAggregationFields
argument_list|(
name|PARSER
argument_list|,
name|parser
lambda|->
name|ParsedBucket
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|,
literal|false
argument_list|)
argument_list|,
name|parser
lambda|->
name|ParsedBucket
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedDateHistogram
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|ParsedDateHistogram
name|aggregation
init|=
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|aggregation
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|aggregation
return|;
block|}
DECL|class|ParsedBucket
specifier|public
specifier|static
class|class
name|ParsedBucket
extends|extends
name|ParsedMultiBucketAggregation
operator|.
name|ParsedBucket
argument_list|<
name|Long
argument_list|>
implements|implements
name|Histogram
operator|.
name|Bucket
block|{
annotation|@
name|Override
DECL|method|getKey
specifier|public
name|Object
name|getKey
parameter_list|()
block|{
return|return
operator|new
name|DateTime
argument_list|(
name|super
operator|.
name|getKey
argument_list|()
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getKeyAsString
specifier|public
name|String
name|getKeyAsString
parameter_list|()
block|{
name|String
name|keyAsString
init|=
name|super
operator|.
name|getKeyAsString
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyAsString
operator|!=
literal|null
condition|)
block|{
return|return
name|keyAsString
return|;
block|}
else|else
block|{
return|return
name|DocValueFormat
operator|.
name|RAW
operator|.
name|format
argument_list|(
operator|(
name|Long
operator|)
name|super
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|fromXContent
specifier|static
name|ParsedBucket
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|boolean
name|keyed
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|parseXContent
argument_list|(
name|parser
argument_list|,
name|keyed
argument_list|,
name|ParsedBucket
operator|::
operator|new
argument_list|,
name|XContentParser
operator|::
name|longValue
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

