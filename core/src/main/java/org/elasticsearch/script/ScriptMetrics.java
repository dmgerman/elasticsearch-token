begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|metrics
operator|.
name|CounterMetric
import|;
end_import

begin_class
DECL|class|ScriptMetrics
specifier|public
class|class
name|ScriptMetrics
block|{
DECL|field|compilationsMetric
specifier|final
name|CounterMetric
name|compilationsMetric
init|=
operator|new
name|CounterMetric
argument_list|()
decl_stmt|;
DECL|field|cacheEvictionsMetric
specifier|final
name|CounterMetric
name|cacheEvictionsMetric
init|=
operator|new
name|CounterMetric
argument_list|()
decl_stmt|;
DECL|method|stats
specifier|public
name|ScriptStats
name|stats
parameter_list|()
block|{
return|return
operator|new
name|ScriptStats
argument_list|(
name|compilationsMetric
operator|.
name|count
argument_list|()
argument_list|,
name|cacheEvictionsMetric
operator|.
name|count
argument_list|()
argument_list|)
return|;
block|}
DECL|method|onCompilation
specifier|public
name|void
name|onCompilation
parameter_list|()
block|{
name|compilationsMetric
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
DECL|method|onCacheEviction
specifier|public
name|void
name|onCacheEviction
parameter_list|()
block|{
name|cacheEvictionsMetric
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

