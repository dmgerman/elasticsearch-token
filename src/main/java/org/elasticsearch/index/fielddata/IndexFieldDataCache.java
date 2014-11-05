begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexReader
import|;
end_import

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
name|index
operator|.
name|mapper
operator|.
name|FieldMapper
import|;
end_import

begin_comment
comment|/**  * A simple field data cache abstraction on the *index* level.  */
end_comment

begin_interface
DECL|interface|IndexFieldDataCache
specifier|public
interface|interface
name|IndexFieldDataCache
block|{
DECL|method|load
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|FD
name|load
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
function_decl|;
DECL|method|load
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
operator|.
name|Global
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|IFD
name|load
parameter_list|(
specifier|final
name|IndexReader
name|indexReader
parameter_list|,
specifier|final
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**      * Clears all the field data stored cached in on this index.      */
DECL|method|clear
name|void
name|clear
parameter_list|()
function_decl|;
comment|/**      * Clears all the field data stored cached in on this index for the specified field name.      */
DECL|method|clear
name|void
name|clear
parameter_list|(
name|String
name|fieldName
parameter_list|)
function_decl|;
DECL|method|clear
name|void
name|clear
parameter_list|(
name|Object
name|coreCacheKey
parameter_list|)
function_decl|;
DECL|interface|Listener
interface|interface
name|Listener
block|{
DECL|method|onLoad
name|void
name|onLoad
parameter_list|(
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|Accountable
name|ramUsage
parameter_list|)
function_decl|;
DECL|method|onUnload
name|void
name|onUnload
parameter_list|(
name|FieldMapper
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
function_decl|;
block|}
DECL|class|None
class|class
name|None
implements|implements
name|IndexFieldDataCache
block|{
annotation|@
name|Override
DECL|method|load
specifier|public
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|FD
name|load
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|indexFieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|load
specifier|public
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
operator|.
name|Global
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|IFD
name|load
parameter_list|(
name|IndexReader
name|indexReader
parameter_list|,
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|(
name|IFD
operator|)
name|indexFieldData
operator|.
name|localGlobalDirect
argument_list|(
name|indexReader
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|Object
name|coreCacheKey
parameter_list|)
block|{          }
block|}
block|}
end_interface

end_unit

