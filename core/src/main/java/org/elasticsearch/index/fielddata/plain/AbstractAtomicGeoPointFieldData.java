begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
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
name|index
operator|.
name|fielddata
operator|.
name|AtomicGeoPointFieldData
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
name|FieldData
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
name|MultiGeoPointValues
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
name|ScriptDocValues
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
name|SortedBinaryDocValues
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
name|Collections
import|;
end_import

begin_class
DECL|class|AbstractAtomicGeoPointFieldData
specifier|public
specifier|abstract
class|class
name|AbstractAtomicGeoPointFieldData
implements|implements
name|AtomicGeoPointFieldData
block|{
annotation|@
name|Override
DECL|method|getBytesValues
specifier|public
specifier|final
name|SortedBinaryDocValues
name|getBytesValues
parameter_list|()
block|{
return|return
name|FieldData
operator|.
name|toString
argument_list|(
name|getGeoPointValues
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getScriptValues
specifier|public
specifier|final
name|ScriptDocValues
operator|.
name|GeoPoints
name|getScriptValues
parameter_list|()
block|{
return|return
operator|new
name|ScriptDocValues
operator|.
name|GeoPoints
argument_list|(
name|getGeoPointValues
argument_list|()
argument_list|)
return|;
block|}
DECL|method|empty
specifier|public
specifier|static
name|AtomicGeoPointFieldData
name|empty
parameter_list|(
specifier|final
name|int
name|maxDoc
parameter_list|)
block|{
return|return
operator|new
name|AbstractAtomicGeoPointFieldData
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{             }
annotation|@
name|Override
specifier|public
name|MultiGeoPointValues
name|getGeoPointValues
parameter_list|()
block|{
return|return
name|FieldData
operator|.
name|emptyMultiGeoPoints
argument_list|(
name|maxDoc
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

