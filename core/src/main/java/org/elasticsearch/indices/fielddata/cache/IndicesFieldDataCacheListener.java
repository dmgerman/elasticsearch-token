begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.fielddata.cache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|fielddata
operator|.
name|cache
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Accountable
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
name|breaker
operator|.
name|CircuitBreaker
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
name|inject
operator|.
name|Inject
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
name|fielddata
operator|.
name|FieldDataType
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
name|fielddata
operator|.
name|IndexFieldDataCache
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
name|FieldMapper
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
name|MappedFieldType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|breaker
operator|.
name|CircuitBreakerService
import|;
end_import

begin_comment
comment|/**  * A {@link IndexFieldDataCache.Listener} implementation that updates indices (node) level statistics / service about  * field data entries being loaded and unloaded.  *  * Currently it only decrements the memory used in the  {@link CircuitBreakerService}.  */
end_comment

begin_class
DECL|class|IndicesFieldDataCacheListener
specifier|public
class|class
name|IndicesFieldDataCacheListener
implements|implements
name|IndexFieldDataCache
operator|.
name|Listener
block|{
DECL|field|circuitBreakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|circuitBreakerService
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndicesFieldDataCacheListener
specifier|public
name|IndicesFieldDataCacheListener
parameter_list|(
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|)
block|{
name|this
operator|.
name|circuitBreakerService
operator|=
name|circuitBreakerService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onLoad
specifier|public
name|void
name|onLoad
parameter_list|(
name|MappedFieldType
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|Accountable
name|fieldData
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|onUnload
specifier|public
name|void
name|onUnload
parameter_list|(
name|MappedFieldType
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|boolean
name|wasEvicted
parameter_list|,
name|long
name|sizeInBytes
parameter_list|)
block|{
assert|assert
name|sizeInBytes
operator|>=
literal|0
operator|:
literal|"When reducing circuit breaker, it should be adjusted with a number higher or equal to 0 and not ["
operator|+
name|sizeInBytes
operator|+
literal|"]"
assert|;
name|circuitBreakerService
operator|.
name|getBreaker
argument_list|(
name|CircuitBreaker
operator|.
name|FIELDDATA
argument_list|)
operator|.
name|addWithoutBreaking
argument_list|(
operator|-
name|sizeInBytes
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
