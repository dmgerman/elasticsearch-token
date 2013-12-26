begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|index
operator|.
name|AtomicReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|settings
operator|.
name|Settings
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
name|Index
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
name|*
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
name|fieldcomparator
operator|.
name|SortMode
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
name|FieldMapper
operator|.
name|Names
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

begin_class
DECL|class|GeoPointBinaryDVIndexFieldData
specifier|public
class|class
name|GeoPointBinaryDVIndexFieldData
extends|extends
name|DocValuesIndexFieldData
implements|implements
name|IndexGeoPointFieldData
argument_list|<
name|AtomicGeoPointFieldData
argument_list|<
name|ScriptDocValues
argument_list|>
argument_list|>
block|{
DECL|method|GeoPointBinaryDVIndexFieldData
specifier|public
name|GeoPointBinaryDVIndexFieldData
parameter_list|(
name|Index
name|index
parameter_list|,
name|Names
name|fieldNames
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|fieldNames
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|valuesOrdered
specifier|public
name|boolean
name|valuesOrdered
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|comparatorSource
specifier|public
specifier|final
name|XFieldComparatorSource
name|comparatorSource
parameter_list|(
annotation|@
name|Nullable
name|Object
name|missingValue
parameter_list|,
name|SortMode
name|sortMode
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"can't sort on geo_point field without using specific sorting feature, like geo_distance"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
name|AtomicGeoPointFieldData
argument_list|<
name|ScriptDocValues
argument_list|>
name|load
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|GeoPointBinaryDVAtomicFieldData
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getBinaryDocValues
argument_list|(
name|fieldNames
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Cannot load doc values"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|loadDirect
specifier|public
name|AtomicGeoPointFieldData
argument_list|<
name|ScriptDocValues
argument_list|>
name|loadDirect
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|load
argument_list|(
name|context
argument_list|)
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
implements|implements
name|IndexFieldData
operator|.
name|Builder
block|{
annotation|@
name|Override
DECL|method|build
specifier|public
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|build
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|)
block|{
specifier|final
name|FieldMapper
operator|.
name|Names
name|fieldNames
init|=
name|mapper
operator|.
name|names
argument_list|()
decl_stmt|;
return|return
operator|new
name|GeoPointBinaryDVIndexFieldData
argument_list|(
name|index
argument_list|,
name|fieldNames
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

