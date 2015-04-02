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
name|elasticsearch
operator|.
name|common
operator|.
name|joda
operator|.
name|DateMathParser
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
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationExecutionException
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
name|internal
operator|.
name|SearchContext
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
name|text
operator|.
name|ParseException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_interface
DECL|interface|ValueParser
specifier|public
interface|interface
name|ValueParser
block|{
DECL|field|IPv4
specifier|static
specifier|final
name|ValueParser
name|IPv4
init|=
operator|new
name|IPv4
argument_list|()
decl_stmt|;
DECL|field|RAW
specifier|static
specifier|final
name|ValueParser
name|RAW
init|=
operator|new
name|Raw
argument_list|()
decl_stmt|;
DECL|field|BOOLEAN
specifier|static
specifier|final
name|ValueParser
name|BOOLEAN
init|=
operator|new
name|Boolean
argument_list|()
decl_stmt|;
DECL|method|parseLong
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
function_decl|;
DECL|method|parseDouble
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
function_decl|;
comment|/**      * Knows how to parse datatime values based on date/time format      */
DECL|class|DateTime
specifier|static
class|class
name|DateTime
implements|implements
name|ValueParser
block|{
DECL|field|DEFAULT
specifier|public
specifier|static
specifier|final
name|DateTime
name|DEFAULT
init|=
operator|new
name|DateTime
argument_list|(
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|)
decl_stmt|;
DECL|field|formatter
specifier|private
name|FormatDateTimeFormatter
name|formatter
decl_stmt|;
DECL|method|DateTime
specifier|public
name|DateTime
parameter_list|(
name|String
name|format
parameter_list|)
block|{
name|this
argument_list|(
name|Joda
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
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
annotation|@
name|Override
DECL|method|parseLong
specifier|public
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseDouble
specifier|public
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|parseLong
argument_list|(
name|value
argument_list|,
name|searchContext
argument_list|)
return|;
block|}
block|}
comment|/**      * Knows how to parse datatime values based on elasticsearch's date math expression      */
DECL|class|DateMath
specifier|static
class|class
name|DateMath
implements|implements
name|ValueParser
block|{
DECL|field|DEFAULT
specifier|public
specifier|static
specifier|final
name|DateMath
name|DEFAULT
init|=
operator|new
name|ValueParser
operator|.
name|DateMath
argument_list|(
operator|new
name|DateMathParser
argument_list|(
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|,
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|TIME_UNIT
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|parser
specifier|private
name|DateMathParser
name|parser
decl_stmt|;
DECL|method|DateMath
specifier|public
name|DateMath
parameter_list|(
name|String
name|format
parameter_list|,
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|DateMathParser
argument_list|(
name|Joda
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
argument_list|,
name|timeUnit
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|DateMath
specifier|public
name|DateMath
parameter_list|(
name|DateMathParser
name|parser
parameter_list|)
block|{
name|this
operator|.
name|parser
operator|=
name|parser
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parseLong
specifier|public
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
specifier|final
name|SearchContext
name|searchContext
parameter_list|)
block|{
specifier|final
name|Callable
argument_list|<
name|Long
argument_list|>
name|now
init|=
operator|new
name|Callable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|searchContext
operator|.
name|nowInMillis
argument_list|()
return|;
block|}
block|}
decl_stmt|;
return|return
name|parser
operator|.
name|parse
argument_list|(
name|value
argument_list|,
name|now
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseDouble
specifier|public
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|parseLong
argument_list|(
name|value
argument_list|,
name|searchContext
argument_list|)
return|;
block|}
DECL|method|mapper
specifier|public
specifier|static
name|DateMath
name|mapper
parameter_list|(
name|DateFieldMapper
name|mapper
parameter_list|)
block|{
return|return
operator|new
name|DateMath
argument_list|(
operator|new
name|DateMathParser
argument_list|(
name|mapper
operator|.
name|dateTimeFormatter
argument_list|()
argument_list|,
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|TIME_UNIT
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**      * Knows how to parse IPv4 formats      */
DECL|class|IPv4
specifier|static
class|class
name|IPv4
implements|implements
name|ValueParser
block|{
DECL|method|IPv4
specifier|private
name|IPv4
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|parseLong
specifier|public
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|IpFieldMapper
operator|.
name|ipToLong
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseDouble
specifier|public
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|parseLong
argument_list|(
name|value
argument_list|,
name|searchContext
argument_list|)
return|;
block|}
block|}
DECL|class|Raw
specifier|static
class|class
name|Raw
implements|implements
name|ValueParser
block|{
DECL|method|Raw
specifier|private
name|Raw
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|parseLong
specifier|public
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseDouble
specifier|public
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
DECL|class|Number
specifier|public
specifier|static
specifier|abstract
class|class
name|Number
implements|implements
name|ValueParser
block|{
DECL|field|format
name|NumberFormat
name|format
decl_stmt|;
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
block|}
annotation|@
name|Override
DECL|method|parseLong
specifier|public
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
try|try
block|{
return|return
name|format
operator|.
name|parse
argument_list|(
name|value
argument_list|)
operator|.
name|longValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|nfe
parameter_list|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"Invalid number format ["
operator|+
operator|(
operator|(
name|DecimalFormat
operator|)
name|format
operator|)
operator|.
name|toPattern
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|parseDouble
specifier|public
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
try|try
block|{
return|return
name|format
operator|.
name|parse
argument_list|(
name|value
argument_list|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|nfe
parameter_list|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"Invalid number format ["
operator|+
operator|(
operator|(
name|DecimalFormat
operator|)
name|format
operator|)
operator|.
name|toPattern
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|class|Boolean
specifier|static
class|class
name|Boolean
implements|implements
name|ValueParser
block|{
DECL|method|Boolean
specifier|private
name|Boolean
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|parseLong
specifier|public
name|long
name|parseLong
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|java
operator|.
name|lang
operator|.
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|value
argument_list|)
condition|?
literal|1
else|:
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|parseDouble
specifier|public
name|double
name|parseDouble
parameter_list|(
name|String
name|value
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
block|{
return|return
name|parseLong
argument_list|(
name|value
argument_list|,
name|searchContext
argument_list|)
return|;
block|}
block|}
block|}
end_interface

end_unit

