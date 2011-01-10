begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.geodistance
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|geodistance
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
name|collect
operator|.
name|ImmutableList
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|unit
operator|.
name|DistanceUnit
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
name|XContentBuilder
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
name|XContentBuilderString
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
name|facet
operator|.
name|Facet
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
name|facet
operator|.
name|InternalFacet
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
name|Iterator
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|InternalGeoDistanceFacet
specifier|public
class|class
name|InternalGeoDistanceFacet
implements|implements
name|GeoDistanceFacet
implements|,
name|InternalFacet
block|{
DECL|field|STREAM_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|STREAM_TYPE
init|=
literal|"geoDistance"
decl_stmt|;
DECL|method|registerStreams
specifier|public
specifier|static
name|void
name|registerStreams
parameter_list|()
block|{
name|Streams
operator|.
name|registerStream
argument_list|(
name|STREAM
argument_list|,
name|STREAM_TYPE
argument_list|)
expr_stmt|;
block|}
DECL|field|STREAM
specifier|static
name|Stream
name|STREAM
init|=
operator|new
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Facet
name|readFacet
parameter_list|(
name|String
name|type
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readGeoDistanceFacet
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|streamType
annotation|@
name|Override
specifier|public
name|String
name|streamType
parameter_list|()
block|{
return|return
name|STREAM_TYPE
return|;
block|}
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|fieldName
specifier|private
name|String
name|fieldName
decl_stmt|;
DECL|field|valueFieldName
specifier|private
name|String
name|valueFieldName
decl_stmt|;
DECL|field|unit
specifier|private
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|entries
name|Entry
index|[]
name|entries
decl_stmt|;
DECL|method|InternalGeoDistanceFacet
name|InternalGeoDistanceFacet
parameter_list|()
block|{     }
DECL|method|InternalGeoDistanceFacet
specifier|public
name|InternalGeoDistanceFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|String
name|valueFieldName
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|,
name|Entry
index|[]
name|entries
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|valueFieldName
operator|=
name|valueFieldName
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|getName
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
argument_list|()
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|getType
annotation|@
name|Override
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
argument_list|()
return|;
block|}
DECL|method|fieldName
annotation|@
name|Override
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
DECL|method|getFieldName
annotation|@
name|Override
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldName
argument_list|()
return|;
block|}
DECL|method|valueFieldName
annotation|@
name|Override
specifier|public
name|String
name|valueFieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|valueFieldName
return|;
block|}
DECL|method|getValueFieldName
annotation|@
name|Override
specifier|public
name|String
name|getValueFieldName
parameter_list|()
block|{
return|return
name|valueFieldName
argument_list|()
return|;
block|}
DECL|method|unit
annotation|@
name|Override
specifier|public
name|DistanceUnit
name|unit
parameter_list|()
block|{
return|return
name|this
operator|.
name|unit
return|;
block|}
DECL|method|getUnit
annotation|@
name|Override
specifier|public
name|DistanceUnit
name|getUnit
parameter_list|()
block|{
return|return
name|unit
argument_list|()
return|;
block|}
DECL|method|entries
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|entries
argument_list|)
return|;
block|}
DECL|method|getEntries
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Entry
argument_list|>
name|getEntries
parameter_list|()
block|{
return|return
name|entries
argument_list|()
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Entry
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|entries
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|readGeoDistanceFacet
specifier|public
specifier|static
name|InternalGeoDistanceFacet
name|readGeoDistanceFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalGeoDistanceFacet
name|facet
init|=
operator|new
name|InternalGeoDistanceFacet
argument_list|()
decl_stmt|;
name|facet
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|facet
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|name
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|fieldName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|valueFieldName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|unit
operator|=
name|DistanceUnit
operator|.
name|readDistanceUnit
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|entries
operator|=
operator|new
name|Entry
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|entries
index|[
name|i
index|]
operator|=
operator|new
name|Entry
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readVLong
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|valueFieldName
argument_list|)
expr_stmt|;
name|DistanceUnit
operator|.
name|writeDistanceUnit
argument_list|(
name|out
argument_list|,
name|unit
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|entries
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|entry
operator|.
name|from
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|entry
operator|.
name|to
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|entry
operator|.
name|count
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|entry
operator|.
name|total
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|_TYPE
specifier|static
specifier|final
name|XContentBuilderString
name|_TYPE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_type"
argument_list|)
decl_stmt|;
DECL|field|_FIELD
specifier|static
specifier|final
name|XContentBuilderString
name|_FIELD
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_field"
argument_list|)
decl_stmt|;
DECL|field|_VALUE_FIELD
specifier|static
specifier|final
name|XContentBuilderString
name|_VALUE_FIELD
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_value_field"
argument_list|)
decl_stmt|;
DECL|field|_UNIT
specifier|static
specifier|final
name|XContentBuilderString
name|_UNIT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_unit"
argument_list|)
decl_stmt|;
DECL|field|RANGES
specifier|static
specifier|final
name|XContentBuilderString
name|RANGES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"ranges"
argument_list|)
decl_stmt|;
DECL|field|FROM
specifier|static
specifier|final
name|XContentBuilderString
name|FROM
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"from"
argument_list|)
decl_stmt|;
DECL|field|TO
specifier|static
specifier|final
name|XContentBuilderString
name|TO
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"to"
argument_list|)
decl_stmt|;
DECL|field|COUNT
specifier|static
specifier|final
name|XContentBuilderString
name|COUNT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
DECL|field|TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total"
argument_list|)
decl_stmt|;
DECL|field|MEAN
specifier|static
specifier|final
name|XContentBuilderString
name|MEAN
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"mean"
argument_list|)
decl_stmt|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
argument_list|,
name|GeoDistanceFacet
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_FIELD
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_VALUE_FIELD
argument_list|,
name|valueFieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_UNIT
argument_list|,
name|unit
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|RANGES
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|entry
operator|.
name|from
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FROM
argument_list|,
name|entry
operator|.
name|from
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|entry
operator|.
name|to
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TO
argument_list|,
name|entry
operator|.
name|to
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|COUNT
argument_list|,
name|entry
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL
argument_list|,
name|entry
operator|.
name|total
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MEAN
argument_list|,
name|entry
operator|.
name|mean
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

