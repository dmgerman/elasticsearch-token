begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|spatial
operator|.
name|util
operator|.
name|GeoHashUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|ParseField
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
name|ParsingException
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
name|Strings
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
name|common
operator|.
name|geo
operator|.
name|GeoUtils
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
name|XContentParser
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
operator|.
name|Token
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
name|index
operator|.
name|mapper
operator|.
name|geo
operator|.
name|BaseGeoPointFieldMapper
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
name|ArrayList
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * A geohash cell filter that filters {@link GeoPoint}s by their geohashes. Basically the a  * Geohash prefix is defined by the filter and all geohashes that are matching this  * prefix will be returned. The<code>neighbors</code> flag allows to filter  * geohashes that surround the given geohash. In general the neighborhood of a  * geohash is defined by its eight adjacent cells.<br>  * The structure of the {@link GeohashCellQuery} is defined as:  *<pre>  *&quot;geohash_bbox&quot; {  *&quot;field&quot;:&quot;location&quot;,  *&quot;geohash&quot;:&quot;u33d8u5dkx8k&quot;,  *&quot;neighbors&quot;:false  * }  *</pre>  */
end_comment

begin_class
DECL|class|GeohashCellQuery
specifier|public
class|class
name|GeohashCellQuery
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geohash_cell"
decl_stmt|;
DECL|field|NEIGHBORS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|NEIGHBORS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"neighbors"
argument_list|)
decl_stmt|;
DECL|field|PRECISION_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|PRECISION_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"precision"
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_NEIGHBORS
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_NEIGHBORS
init|=
literal|false
decl_stmt|;
comment|/**      * Create a new geohash filter for a given set of geohashes. In general this method      * returns a boolean filter combining the geohashes OR-wise.      *      * @param context     Context of the filter      * @param fieldType field mapper for geopoints      * @param geohash     mandatory geohash      * @param geohashes   optional array of additional geohashes      * @return a new GeoBoundinboxfilter      */
DECL|method|create
specifier|public
specifier|static
name|Query
name|create
parameter_list|(
name|QueryShardContext
name|context
parameter_list|,
name|BaseGeoPointFieldMapper
operator|.
name|GeoPointFieldType
name|fieldType
parameter_list|,
name|String
name|geohash
parameter_list|,
annotation|@
name|Nullable
name|List
argument_list|<
name|CharSequence
argument_list|>
name|geohashes
parameter_list|)
block|{
name|MappedFieldType
name|geoHashMapper
init|=
name|fieldType
operator|.
name|geoHashFieldType
argument_list|()
decl_stmt|;
if|if
condition|(
name|geoHashMapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"geohash filter needs geohash_prefix to be enabled"
argument_list|)
throw|;
block|}
if|if
condition|(
name|geohashes
operator|==
literal|null
operator|||
name|geohashes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|geoHashMapper
operator|.
name|termQuery
argument_list|(
name|geohash
argument_list|,
name|context
argument_list|)
return|;
block|}
else|else
block|{
name|geohashes
operator|.
name|add
argument_list|(
name|geohash
argument_list|)
expr_stmt|;
return|return
name|geoHashMapper
operator|.
name|termsQuery
argument_list|(
name|geohashes
argument_list|,
name|context
argument_list|)
return|;
block|}
block|}
comment|/**      * Builder for a geohashfilter. It needs the fields<code>fieldname</code> and      *<code>geohash</code> to be set. the default for a neighbor filteing is      *<code>false</code>.      */
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|Builder
argument_list|>
block|{
comment|// we need to store the geohash rather than the corresponding point,
comment|// because a transformation from a geohash to a point an back to the
comment|// geohash will extend the accuracy of the hash to max precision
comment|// i.e. by filing up with z's.
DECL|field|fieldName
specifier|private
name|String
name|fieldName
decl_stmt|;
DECL|field|geohash
specifier|private
name|String
name|geohash
decl_stmt|;
DECL|field|levels
specifier|private
name|Integer
name|levels
init|=
literal|null
decl_stmt|;
DECL|field|neighbors
specifier|private
name|boolean
name|neighbors
init|=
name|DEFAULT_NEIGHBORS
decl_stmt|;
DECL|field|PROTOTYPE
specifier|private
specifier|static
specifier|final
name|Builder
name|PROTOTYPE
init|=
operator|new
name|Builder
argument_list|(
literal|"field"
argument_list|,
operator|new
name|GeoPoint
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|field
parameter_list|,
name|GeoPoint
name|point
parameter_list|)
block|{
name|this
argument_list|(
name|field
argument_list|,
name|point
operator|==
literal|null
condition|?
literal|null
else|:
name|point
operator|.
name|geohash
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|field
parameter_list|,
name|String
name|geohash
parameter_list|)
block|{
name|this
argument_list|(
name|field
argument_list|,
name|geohash
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|field
parameter_list|,
name|String
name|geohash
parameter_list|,
name|boolean
name|neighbors
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|isEmpty
argument_list|(
name|field
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"fieldName must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|Strings
operator|.
name|isEmpty
argument_list|(
name|geohash
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"geohash or point must be defined"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|geohash
operator|=
name|geohash
expr_stmt|;
name|this
operator|.
name|neighbors
operator|=
name|neighbors
expr_stmt|;
block|}
DECL|method|point
specifier|public
name|Builder
name|point
parameter_list|(
name|GeoPoint
name|point
parameter_list|)
block|{
name|this
operator|.
name|geohash
operator|=
name|point
operator|.
name|getGeohash
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|point
specifier|public
name|Builder
name|point
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|this
operator|.
name|geohash
operator|=
name|GeoHashUtils
operator|.
name|stringEncode
argument_list|(
name|lon
argument_list|,
name|lat
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|geohash
specifier|public
name|Builder
name|geohash
parameter_list|(
name|String
name|geohash
parameter_list|)
block|{
name|this
operator|.
name|geohash
operator|=
name|geohash
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|geohash
specifier|public
name|String
name|geohash
parameter_list|()
block|{
return|return
name|geohash
return|;
block|}
DECL|method|precision
specifier|public
name|Builder
name|precision
parameter_list|(
name|int
name|levels
parameter_list|)
block|{
if|if
condition|(
name|levels
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"precision must be greater than 0. Found ["
operator|+
name|levels
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|levels
operator|=
name|levels
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|precision
specifier|public
name|Integer
name|precision
parameter_list|()
block|{
return|return
name|levels
return|;
block|}
DECL|method|precision
specifier|public
name|Builder
name|precision
parameter_list|(
name|String
name|precision
parameter_list|)
block|{
name|double
name|meters
init|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
name|precision
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
decl_stmt|;
return|return
name|precision
argument_list|(
name|GeoUtils
operator|.
name|geoHashLevelsForPrecision
argument_list|(
name|meters
argument_list|)
argument_list|)
return|;
block|}
DECL|method|neighbors
specifier|public
name|Builder
name|neighbors
parameter_list|(
name|boolean
name|neighbors
parameter_list|)
block|{
name|this
operator|.
name|neighbors
operator|=
name|neighbors
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|neighbors
specifier|public
name|boolean
name|neighbors
parameter_list|()
block|{
return|return
name|neighbors
return|;
block|}
DECL|method|fieldName
specifier|public
name|Builder
name|fieldName
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
annotation|@
name|Override
DECL|method|doToQuery
specifier|protected
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"failed to parse [{}] query. missing [{}] field [{}]"
argument_list|,
name|NAME
argument_list|,
name|BaseGeoPointFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
name|fieldName
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
operator|(
name|fieldType
operator|instanceof
name|BaseGeoPointFieldMapper
operator|.
name|GeoPointFieldType
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"failed to parse [{}] query. field [{}] is not a geo_point field"
argument_list|,
name|NAME
argument_list|,
name|fieldName
argument_list|)
throw|;
block|}
name|BaseGeoPointFieldMapper
operator|.
name|GeoPointFieldType
name|geoFieldType
init|=
operator|(
operator|(
name|BaseGeoPointFieldMapper
operator|.
name|GeoPointFieldType
operator|)
name|fieldType
operator|)
decl_stmt|;
if|if
condition|(
operator|!
name|geoFieldType
operator|.
name|isGeoHashPrefixEnabled
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"failed to parse [{}] query. [geohash_prefix] is not enabled for field [{}]"
argument_list|,
name|NAME
argument_list|,
name|fieldName
argument_list|)
throw|;
block|}
name|String
name|geohash
init|=
name|this
operator|.
name|geohash
decl_stmt|;
if|if
condition|(
name|levels
operator|!=
literal|null
condition|)
block|{
name|int
name|len
init|=
name|Math
operator|.
name|min
argument_list|(
name|levels
argument_list|,
name|geohash
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|geohash
operator|=
name|geohash
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
name|Query
name|query
decl_stmt|;
if|if
condition|(
name|neighbors
condition|)
block|{
name|query
operator|=
name|create
argument_list|(
name|context
argument_list|,
name|geoFieldType
argument_list|,
name|geohash
argument_list|,
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
name|geohash
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|CharSequence
argument_list|>
argument_list|(
literal|8
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
name|create
argument_list|(
name|context
argument_list|,
name|geoFieldType
argument_list|,
name|geohash
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
name|void
name|doXContent
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
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|NEIGHBORS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|neighbors
argument_list|)
expr_stmt|;
if|if
condition|(
name|levels
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|PRECISION_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|levels
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|fieldName
argument_list|,
name|geohash
argument_list|)
expr_stmt|;
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|Builder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|field
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|String
name|geohash
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|field
argument_list|,
name|geohash
argument_list|)
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|precision
argument_list|(
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|neighbors
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|geohash
argument_list|)
expr_stmt|;
name|boolean
name|hasLevels
init|=
name|levels
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasLevels
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasLevels
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|levels
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|neighbors
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Builder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldName
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|geohash
argument_list|,
name|other
operator|.
name|geohash
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|levels
argument_list|,
name|other
operator|.
name|levels
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|neighbors
argument_list|,
name|other
operator|.
name|neighbors
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|fieldName
argument_list|,
name|geohash
argument_list|,
name|levels
argument_list|,
name|neighbors
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
DECL|class|Parser
specifier|public
specifier|static
class|class
name|Parser
implements|implements
name|QueryParser
argument_list|<
name|Builder
argument_list|>
block|{
DECL|field|QUERY_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|QUERY_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
annotation|@
name|Inject
DECL|method|Parser
specifier|public
name|Parser
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|Builder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|String
name|geohash
init|=
literal|null
decl_stmt|;
name|Integer
name|levels
init|=
literal|null
decl_stmt|;
name|Boolean
name|neighbors
init|=
literal|null
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|Float
name|boost
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
if|if
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|currentToken
argument_list|()
operator|)
operator|!=
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse [{}] query. expected an object but found [{}] instead"
argument_list|,
name|NAME
argument_list|,
name|token
argument_list|)
throw|;
block|}
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|String
name|field
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
name|parseContext
operator|.
name|isDeprecatedSetting
argument_list|(
name|field
argument_list|)
condition|)
block|{
comment|// skip
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|field
argument_list|,
name|PRECISION_FIELD
argument_list|)
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
name|levels
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|double
name|meters
init|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
decl_stmt|;
name|levels
operator|=
name|GeoUtils
operator|.
name|geoHashLevelsForPrecision
argument_list|(
name|meters
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|field
argument_list|,
name|NEIGHBORS_FIELD
argument_list|)
condition|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|neighbors
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|field
argument_list|,
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
argument_list|)
condition|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|field
argument_list|,
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
argument_list|)
condition|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
name|fieldName
operator|=
name|field
expr_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
comment|// A string indicates either a geohash or a
comment|// lat/lon
comment|// string
name|String
name|location
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|.
name|indexOf
argument_list|(
literal|","
argument_list|)
operator|>
literal|0
condition|)
block|{
name|geohash
operator|=
name|GeoUtils
operator|.
name|parseGeoPoint
argument_list|(
name|parser
argument_list|)
operator|.
name|geohash
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|geohash
operator|=
name|location
expr_stmt|;
block|}
block|}
else|else
block|{
name|geohash
operator|=
name|GeoUtils
operator|.
name|parseGeoPoint
argument_list|(
name|parser
argument_list|)
operator|.
name|geohash
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] field name already set to ["
operator|+
name|fieldName
operator|+
literal|"] but found ["
operator|+
name|field
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse [{}] query. unexpected token [{}]"
argument_list|,
name|NAME
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|fieldName
argument_list|,
name|geohash
argument_list|)
decl_stmt|;
if|if
condition|(
name|levels
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|precision
argument_list|(
name|levels
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|neighbors
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|neighbors
argument_list|(
name|neighbors
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|GeohashCellQuery
operator|.
name|Builder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|Builder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
block|}
end_class

end_unit

