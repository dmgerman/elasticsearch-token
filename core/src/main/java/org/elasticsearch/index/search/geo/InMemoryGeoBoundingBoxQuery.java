begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|geo
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
name|search
operator|.
name|IndexSearcher
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
name|search
operator|.
name|Query
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
name|search
operator|.
name|RandomAccessWeight
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
name|search
operator|.
name|Weight
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
name|Bits
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
name|geo
operator|.
name|GeoPoint
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
name|IndexGeoPointFieldData
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
name|Objects
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InMemoryGeoBoundingBoxQuery
specifier|public
class|class
name|InMemoryGeoBoundingBoxQuery
extends|extends
name|Query
block|{
DECL|field|topLeft
specifier|private
specifier|final
name|GeoPoint
name|topLeft
decl_stmt|;
DECL|field|bottomRight
specifier|private
specifier|final
name|GeoPoint
name|bottomRight
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexGeoPointFieldData
name|indexFieldData
decl_stmt|;
DECL|method|InMemoryGeoBoundingBoxQuery
specifier|public
name|InMemoryGeoBoundingBoxQuery
parameter_list|(
name|GeoPoint
name|topLeft
parameter_list|,
name|GeoPoint
name|bottomRight
parameter_list|,
name|IndexGeoPointFieldData
name|indexFieldData
parameter_list|)
block|{
name|this
operator|.
name|topLeft
operator|=
name|topLeft
expr_stmt|;
name|this
operator|.
name|bottomRight
operator|=
name|bottomRight
expr_stmt|;
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
block|}
DECL|method|topLeft
specifier|public
name|GeoPoint
name|topLeft
parameter_list|()
block|{
return|return
name|topLeft
return|;
block|}
DECL|method|bottomRight
specifier|public
name|GeoPoint
name|bottomRight
parameter_list|()
block|{
return|return
name|bottomRight
return|;
block|}
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RandomAccessWeight
argument_list|(
name|this
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Bits
name|getMatchingDocs
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|maxDoc
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
specifier|final
name|MultiGeoPointValues
name|values
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getGeoPointValues
argument_list|()
decl_stmt|;
comment|// checks to see if bounding box crosses 180 degrees
if|if
condition|(
name|topLeft
operator|.
name|lon
argument_list|()
operator|>
name|bottomRight
operator|.
name|lon
argument_list|()
condition|)
block|{
return|return
operator|new
name|Meridian180GeoBoundingBoxBits
argument_list|(
name|maxDoc
argument_list|,
name|values
argument_list|,
name|topLeft
argument_list|,
name|bottomRight
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|GeoBoundingBoxBits
argument_list|(
name|maxDoc
argument_list|,
name|values
argument_list|,
name|topLeft
argument_list|,
name|bottomRight
argument_list|)
return|;
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
literal|"GeoBoundingBoxFilter("
operator|+
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
operator|+
literal|", "
operator|+
name|topLeft
operator|+
literal|", "
operator|+
name|bottomRight
operator|+
literal|")"
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|InMemoryGeoBoundingBoxQuery
name|other
init|=
operator|(
name|InMemoryGeoBoundingBoxQuery
operator|)
name|obj
decl_stmt|;
return|return
name|fieldName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|other
operator|.
name|fieldName
argument_list|()
argument_list|)
operator|&&
name|topLeft
operator|.
name|equals
argument_list|(
name|other
operator|.
name|topLeft
argument_list|)
operator|&&
name|bottomRight
operator|.
name|equals
argument_list|(
name|other
operator|.
name|bottomRight
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
name|Objects
operator|.
name|hash
argument_list|(
name|super
operator|.
name|hashCode
argument_list|()
argument_list|,
name|fieldName
argument_list|()
argument_list|,
name|topLeft
argument_list|,
name|bottomRight
argument_list|)
return|;
block|}
DECL|class|Meridian180GeoBoundingBoxBits
specifier|private
specifier|static
class|class
name|Meridian180GeoBoundingBoxBits
implements|implements
name|Bits
block|{
DECL|field|maxDoc
specifier|private
specifier|final
name|int
name|maxDoc
decl_stmt|;
DECL|field|values
specifier|private
specifier|final
name|MultiGeoPointValues
name|values
decl_stmt|;
DECL|field|topLeft
specifier|private
specifier|final
name|GeoPoint
name|topLeft
decl_stmt|;
DECL|field|bottomRight
specifier|private
specifier|final
name|GeoPoint
name|bottomRight
decl_stmt|;
DECL|method|Meridian180GeoBoundingBoxBits
specifier|public
name|Meridian180GeoBoundingBoxBits
parameter_list|(
name|int
name|maxDoc
parameter_list|,
name|MultiGeoPointValues
name|values
parameter_list|,
name|GeoPoint
name|topLeft
parameter_list|,
name|GeoPoint
name|bottomRight
parameter_list|)
block|{
name|this
operator|.
name|maxDoc
operator|=
name|maxDoc
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|topLeft
operator|=
name|topLeft
expr_stmt|;
name|this
operator|.
name|bottomRight
operator|=
name|bottomRight
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|length
init|=
name|values
operator|.
name|count
argument_list|()
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|GeoPoint
name|point
init|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
operator|(
name|topLeft
operator|.
name|lon
argument_list|()
operator|<=
name|point
operator|.
name|lon
argument_list|()
operator|||
name|bottomRight
operator|.
name|lon
argument_list|()
operator|>=
name|point
operator|.
name|lon
argument_list|()
operator|)
operator|)
operator|&&
operator|(
name|topLeft
operator|.
name|lat
argument_list|()
operator|>=
name|point
operator|.
name|lat
argument_list|()
operator|&&
name|bottomRight
operator|.
name|lat
argument_list|()
operator|<=
name|point
operator|.
name|lat
argument_list|()
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|length
specifier|public
name|int
name|length
parameter_list|()
block|{
return|return
name|maxDoc
return|;
block|}
block|}
DECL|class|GeoBoundingBoxBits
specifier|private
specifier|static
class|class
name|GeoBoundingBoxBits
implements|implements
name|Bits
block|{
DECL|field|maxDoc
specifier|private
specifier|final
name|int
name|maxDoc
decl_stmt|;
DECL|field|values
specifier|private
specifier|final
name|MultiGeoPointValues
name|values
decl_stmt|;
DECL|field|topLeft
specifier|private
specifier|final
name|GeoPoint
name|topLeft
decl_stmt|;
DECL|field|bottomRight
specifier|private
specifier|final
name|GeoPoint
name|bottomRight
decl_stmt|;
DECL|method|GeoBoundingBoxBits
specifier|public
name|GeoBoundingBoxBits
parameter_list|(
name|int
name|maxDoc
parameter_list|,
name|MultiGeoPointValues
name|values
parameter_list|,
name|GeoPoint
name|topLeft
parameter_list|,
name|GeoPoint
name|bottomRight
parameter_list|)
block|{
name|this
operator|.
name|maxDoc
operator|=
name|maxDoc
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|topLeft
operator|=
name|topLeft
expr_stmt|;
name|this
operator|.
name|bottomRight
operator|=
name|bottomRight
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|length
init|=
name|values
operator|.
name|count
argument_list|()
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|GeoPoint
name|point
init|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|topLeft
operator|.
name|lon
argument_list|()
operator|<=
name|point
operator|.
name|lon
argument_list|()
operator|&&
name|bottomRight
operator|.
name|lon
argument_list|()
operator|>=
name|point
operator|.
name|lon
argument_list|()
operator|&&
name|topLeft
operator|.
name|lat
argument_list|()
operator|>=
name|point
operator|.
name|lat
argument_list|()
operator|&&
name|bottomRight
operator|.
name|lat
argument_list|()
operator|<=
name|point
operator|.
name|lat
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|length
specifier|public
name|int
name|length
parameter_list|()
block|{
return|return
name|maxDoc
return|;
block|}
block|}
block|}
end_class

end_unit

