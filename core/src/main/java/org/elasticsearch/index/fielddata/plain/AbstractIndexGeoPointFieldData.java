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
name|spatial
operator|.
name|geopoint
operator|.
name|document
operator|.
name|GeoPointField
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
name|spatial
operator|.
name|util
operator|.
name|GeoEncodingUtils
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
name|BytesRef
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
name|BytesRefIterator
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
name|CharsRefBuilder
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
name|LegacyNumericUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|IndexSettings
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
name|IndexFieldData
operator|.
name|XFieldComparatorSource
operator|.
name|Nested
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
name|search
operator|.
name|MultiValueMode
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
DECL|class|AbstractIndexGeoPointFieldData
specifier|abstract
class|class
name|AbstractIndexGeoPointFieldData
extends|extends
name|AbstractIndexFieldData
argument_list|<
name|AtomicGeoPointFieldData
argument_list|>
implements|implements
name|IndexGeoPointFieldData
block|{
DECL|class|BaseGeoPointTermsEnum
specifier|protected
specifier|abstract
specifier|static
class|class
name|BaseGeoPointTermsEnum
block|{
DECL|field|termsEnum
specifier|protected
specifier|final
name|BytesRefIterator
name|termsEnum
decl_stmt|;
DECL|method|BaseGeoPointTermsEnum
specifier|protected
name|BaseGeoPointTermsEnum
parameter_list|(
name|BytesRefIterator
name|termsEnum
parameter_list|)
block|{
name|this
operator|.
name|termsEnum
operator|=
name|termsEnum
expr_stmt|;
block|}
block|}
DECL|class|GeoPointTermsEnum
specifier|protected
specifier|static
class|class
name|GeoPointTermsEnum
extends|extends
name|BaseGeoPointTermsEnum
block|{
DECL|field|termEncoding
specifier|private
specifier|final
name|GeoPointField
operator|.
name|TermEncoding
name|termEncoding
decl_stmt|;
DECL|method|GeoPointTermsEnum
specifier|protected
name|GeoPointTermsEnum
parameter_list|(
name|BytesRefIterator
name|termsEnum
parameter_list|,
name|GeoPointField
operator|.
name|TermEncoding
name|termEncoding
parameter_list|)
block|{
name|super
argument_list|(
name|termsEnum
argument_list|)
expr_stmt|;
name|this
operator|.
name|termEncoding
operator|=
name|termEncoding
expr_stmt|;
block|}
DECL|method|next
specifier|public
name|Long
name|next
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|BytesRef
name|term
init|=
name|termsEnum
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|termEncoding
operator|==
name|GeoPointField
operator|.
name|TermEncoding
operator|.
name|PREFIX
condition|)
block|{
return|return
name|GeoEncodingUtils
operator|.
name|prefixCodedToGeoCoded
argument_list|(
name|term
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|termEncoding
operator|==
name|GeoPointField
operator|.
name|TermEncoding
operator|.
name|NUMERIC
condition|)
block|{
return|return
name|LegacyNumericUtils
operator|.
name|prefixCodedToLong
argument_list|(
name|term
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"GeoPoint.TermEncoding should be one of: "
operator|+
name|GeoPointField
operator|.
name|TermEncoding
operator|.
name|PREFIX
operator|+
literal|" or "
operator|+
name|GeoPointField
operator|.
name|TermEncoding
operator|.
name|NUMERIC
operator|+
literal|" found: "
operator|+
name|termEncoding
argument_list|)
throw|;
block|}
block|}
DECL|class|GeoPointTermsEnumLegacy
specifier|protected
specifier|static
class|class
name|GeoPointTermsEnumLegacy
extends|extends
name|BaseGeoPointTermsEnum
block|{
DECL|field|next
specifier|private
specifier|final
name|GeoPoint
name|next
decl_stmt|;
DECL|field|spare
specifier|private
specifier|final
name|CharsRefBuilder
name|spare
decl_stmt|;
DECL|method|GeoPointTermsEnumLegacy
specifier|protected
name|GeoPointTermsEnumLegacy
parameter_list|(
name|BytesRefIterator
name|termsEnum
parameter_list|)
block|{
name|super
argument_list|(
name|termsEnum
argument_list|)
expr_stmt|;
name|next
operator|=
operator|new
name|GeoPoint
argument_list|()
expr_stmt|;
name|spare
operator|=
operator|new
name|CharsRefBuilder
argument_list|()
expr_stmt|;
block|}
DECL|method|next
specifier|public
name|GeoPoint
name|next
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|BytesRef
name|term
init|=
name|termsEnum
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|term
argument_list|)
expr_stmt|;
name|int
name|commaIndex
init|=
operator|-
literal|1
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
name|spare
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|spare
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|','
condition|)
block|{
comment|// saves a string creation
name|commaIndex
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|commaIndex
operator|==
operator|-
literal|1
condition|)
block|{
assert|assert
literal|false
assert|;
return|return
name|next
operator|.
name|reset
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|final
name|double
name|lat
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
operator|new
name|String
argument_list|(
name|spare
operator|.
name|chars
argument_list|()
argument_list|,
literal|0
argument_list|,
name|commaIndex
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|double
name|lon
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
operator|new
name|String
argument_list|(
name|spare
operator|.
name|chars
argument_list|()
argument_list|,
name|commaIndex
operator|+
literal|1
argument_list|,
name|spare
operator|.
name|length
argument_list|()
operator|-
operator|(
name|commaIndex
operator|+
literal|1
operator|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|next
operator|.
name|reset
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
block|}
DECL|method|AbstractIndexGeoPointFieldData
specifier|public
name|AbstractIndexGeoPointFieldData
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|fieldName
argument_list|,
name|fieldDataType
argument_list|,
name|cache
argument_list|)
expr_stmt|;
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
name|MultiValueMode
name|sortMode
parameter_list|,
name|Nested
name|nested
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"can't sort on geo_point field without using specific sorting feature, like geo_distance"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|empty
specifier|protected
name|AtomicGeoPointFieldData
name|empty
parameter_list|(
name|int
name|maxDoc
parameter_list|)
block|{
return|return
name|AbstractAtomicGeoPointFieldData
operator|.
name|empty
argument_list|(
name|maxDoc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

