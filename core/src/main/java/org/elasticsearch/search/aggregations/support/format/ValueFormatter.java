begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.support.format
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|format
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
name|XGeoHashUtils
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|joda
operator|.
name|FormatDateTimeFormatter
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
name|joda
operator|.
name|Joda
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
name|core
operator|.
name|DateFieldMapper
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
name|ip
operator|.
name|IpFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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
name|text
operator|.
name|DecimalFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DecimalFormatSymbols
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|NumberFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_comment
comment|/**  * A strategy for formatting time represented as millis long value to string  */
end_comment

begin_interface
DECL|interface|ValueFormatter
specifier|public
interface|interface
name|ValueFormatter
extends|extends
name|Streamable
block|{
DECL|field|RAW
specifier|public
specifier|final
specifier|static
name|ValueFormatter
name|RAW
init|=
operator|new
name|Raw
argument_list|()
decl_stmt|;
DECL|field|IPv4
specifier|public
specifier|final
specifier|static
name|ValueFormatter
name|IPv4
init|=
operator|new
name|IPv4Formatter
argument_list|()
decl_stmt|;
DECL|field|GEOHASH
specifier|public
specifier|final
specifier|static
name|ValueFormatter
name|GEOHASH
init|=
operator|new
name|GeoHash
argument_list|()
decl_stmt|;
DECL|field|BOOLEAN
specifier|public
specifier|final
specifier|static
name|ValueFormatter
name|BOOLEAN
init|=
operator|new
name|BooleanFormatter
argument_list|()
decl_stmt|;
comment|/**      * Uniquely identifies this formatter (used for efficient serialization)      *      * @return  The id of this formatter      */
DECL|method|id
name|byte
name|id
parameter_list|()
function_decl|;
comment|/**      * Formats the given millis time value (since the epoch) to string.      *      * @param value The long value to format.      * @return      The formatted value as string.      */
DECL|method|format
name|String
name|format
parameter_list|(
name|long
name|value
parameter_list|)
function_decl|;
comment|/**      * @param value double The double value to format.      * @return      The formatted value as string      */
DECL|method|format
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
function_decl|;
DECL|class|Raw
specifier|static
class|class
name|Raw
implements|implements
name|ValueFormatter
block|{
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|1
decl_stmt|;
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{         }
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{         }
block|}
comment|/**      * A time formatter which is based on date/time format.      */
DECL|class|DateTime
specifier|public
specifier|static
class|class
name|DateTime
implements|implements
name|ValueFormatter
block|{
DECL|field|DEFAULT
specifier|public
specifier|static
specifier|final
name|ValueFormatter
name|DEFAULT
init|=
operator|new
name|ValueFormatter
operator|.
name|DateTime
argument_list|(
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|)
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
init|=
name|DateTimeZone
operator|.
name|UTC
decl_stmt|;
DECL|method|mapper
specifier|public
specifier|static
name|DateTime
name|mapper
parameter_list|(
name|DateFieldMapper
operator|.
name|DateFieldType
name|fieldType
parameter_list|,
name|DateTimeZone
name|timezone
parameter_list|)
block|{
return|return
operator|new
name|DateTime
argument_list|(
name|fieldType
operator|.
name|dateTimeFormatter
argument_list|()
argument_list|,
name|timezone
argument_list|)
return|;
block|}
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|2
decl_stmt|;
DECL|field|formatter
name|FormatDateTimeFormatter
name|formatter
decl_stmt|;
DECL|method|DateTime
name|DateTime
parameter_list|()
block|{}
comment|// for serialization
DECL|method|DateTime
specifier|public
name|DateTime
parameter_list|(
name|String
name|format
parameter_list|)
block|{
name|this
operator|.
name|formatter
operator|=
name|Joda
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
expr_stmt|;
block|}
DECL|method|DateTime
specifier|public
name|DateTime
parameter_list|(
name|FormatDateTimeFormatter
name|formatter
parameter_list|)
block|{
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
block|}
DECL|method|DateTime
specifier|public
name|DateTime
parameter_list|(
name|String
name|format
parameter_list|,
name|DateTimeZone
name|timezone
parameter_list|)
block|{
name|this
operator|.
name|formatter
operator|=
name|Joda
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeZone
operator|=
name|timezone
operator|!=
literal|null
condition|?
name|timezone
else|:
name|DateTimeZone
operator|.
name|UTC
expr_stmt|;
block|}
DECL|method|DateTime
specifier|public
name|DateTime
parameter_list|(
name|FormatDateTimeFormatter
name|formatter
parameter_list|,
name|DateTimeZone
name|timezone
parameter_list|)
block|{
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
name|this
operator|.
name|timeZone
operator|=
name|timezone
operator|!=
literal|null
condition|?
name|timezone
else|:
name|DateTimeZone
operator|.
name|UTC
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|long
name|time
parameter_list|)
block|{
return|return
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|withZone
argument_list|(
name|timeZone
argument_list|)
operator|.
name|print
argument_list|(
name|time
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|format
argument_list|(
operator|(
name|long
operator|)
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
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
name|formatter
operator|=
name|Joda
operator|.
name|forPattern
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|writeString
argument_list|(
name|formatter
operator|.
name|format
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|timeZone
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Number
specifier|public
specifier|static
specifier|abstract
class|class
name|Number
implements|implements
name|ValueFormatter
block|{
DECL|field|format
name|NumberFormat
name|format
decl_stmt|;
DECL|method|Number
name|Number
parameter_list|()
block|{}
comment|// for serialization
DECL|method|Number
name|Number
parameter_list|(
name|NumberFormat
name|format
parameter_list|)
block|{
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|format
operator|.
name|format
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|format
operator|.
name|format
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|class|Pattern
specifier|public
specifier|static
class|class
name|Pattern
extends|extends
name|Number
block|{
DECL|field|SYMBOLS
specifier|private
specifier|static
specifier|final
name|DecimalFormatSymbols
name|SYMBOLS
init|=
operator|new
name|DecimalFormatSymbols
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|4
decl_stmt|;
DECL|field|pattern
name|String
name|pattern
decl_stmt|;
DECL|method|Pattern
name|Pattern
parameter_list|()
block|{}
comment|// for serialization
DECL|method|Pattern
specifier|public
name|Pattern
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|DecimalFormat
argument_list|(
name|pattern
argument_list|,
name|SYMBOLS
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
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
name|pattern
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|format
operator|=
operator|new
name|DecimalFormat
argument_list|(
name|pattern
argument_list|,
name|SYMBOLS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|writeString
argument_list|(
name|pattern
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|IPv4Formatter
specifier|static
class|class
name|IPv4Formatter
implements|implements
name|ValueFormatter
block|{
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|6
decl_stmt|;
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|IpFieldMapper
operator|.
name|longToIp
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|format
argument_list|(
operator|(
name|long
operator|)
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{         }
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{         }
block|}
DECL|class|GeoHash
specifier|static
class|class
name|GeoHash
implements|implements
name|ValueFormatter
block|{
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|8
decl_stmt|;
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|XGeoHashUtils
operator|.
name|stringEncode
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|format
argument_list|(
operator|(
name|long
operator|)
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{          }
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{          }
block|}
DECL|class|BooleanFormatter
specifier|static
class|class
name|BooleanFormatter
implements|implements
name|ValueFormatter
block|{
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|10
decl_stmt|;
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
operator|!=
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
operator|!=
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{         }
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{         }
block|}
block|}
end_interface

end_unit

