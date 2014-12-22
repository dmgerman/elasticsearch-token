begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.ipv4
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
name|range
operator|.
name|ipv4
package|;
end_package

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
name|bucket
operator|.
name|range
operator|.
name|Range
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
comment|/**  * A range aggregation on ipv4 values.  */
end_comment

begin_interface
DECL|interface|IPv4Range
specifier|public
interface|interface
name|IPv4Range
extends|extends
name|Range
block|{
DECL|interface|Bucket
specifier|static
interface|interface
name|Bucket
extends|extends
name|Range
operator|.
name|Bucket
block|{      }
annotation|@
name|Override
DECL|method|getBuckets
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|getBuckets
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|getBucketByKey
name|IPv4Range
operator|.
name|Bucket
name|getBucketByKey
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

