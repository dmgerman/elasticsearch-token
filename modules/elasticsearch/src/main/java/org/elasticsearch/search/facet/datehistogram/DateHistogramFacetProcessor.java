begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.datehistogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|datehistogram
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
name|ImmutableMap
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
name|collect
operator|.
name|MapBuilder
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
name|component
operator|.
name|AbstractComponent
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
name|joda
operator|.
name|time
operator|.
name|Chronology
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
name|time
operator|.
name|DateTimeField
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
name|time
operator|.
name|DateTimeZone
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
name|time
operator|.
name|MutableDateTime
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
name|common
operator|.
name|trove
operator|.
name|impl
operator|.
name|Constants
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
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TObjectIntHashMap
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
name|TimeValue
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
name|index
operator|.
name|field
operator|.
name|data
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
name|FacetCollector
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
name|FacetPhaseExecutionException
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
name|FacetProcessor
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|DateHistogramFacetProcessor
specifier|public
class|class
name|DateHistogramFacetProcessor
extends|extends
name|AbstractComponent
implements|implements
name|FacetProcessor
block|{
DECL|field|dateFieldParsers
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|DateFieldParser
argument_list|>
name|dateFieldParsers
decl_stmt|;
DECL|field|rounding
specifier|private
specifier|final
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|rounding
init|=
operator|new
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
argument_list|(
name|Constants
operator|.
name|DEFAULT_CAPACITY
argument_list|,
name|Constants
operator|.
name|DEFAULT_LOAD_FACTOR
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
DECL|method|DateHistogramFacetProcessor
annotation|@
name|Inject
specifier|public
name|DateHistogramFacetProcessor
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|InternalDateHistogramFacet
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|dateFieldParsers
operator|=
name|MapBuilder
operator|.
expr|<
name|String
operator|,
name|DateFieldParser
operator|>
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"year"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|YearOfCentury
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1y"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|YearOfCentury
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"month"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|MonthOfYear
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1m"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|MonthOfYear
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"week"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|WeekOfWeekyear
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1w"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|WeekOfWeekyear
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"day"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|DayOfMonth
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1d"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|DayOfMonth
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"hour"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|HourOfDay
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1h"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|HourOfDay
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"minute"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|MinuteOfHour
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1m"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|MinuteOfHour
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"second"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|SecondOfMinute
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"1s"
argument_list|,
operator|new
name|DateFieldParser
operator|.
name|SecondOfMinute
argument_list|()
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"floor"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"ceiling"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_CEILING
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"half_even"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_HALF_EVEN
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"halfEven"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_HALF_EVEN
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"half_floor"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_HALF_FLOOR
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"halfFloor"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_HALF_FLOOR
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"half_ceiling"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_HALF_CEILING
argument_list|)
expr_stmt|;
name|rounding
operator|.
name|put
argument_list|(
literal|"halfCeiling"
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_HALF_CEILING
argument_list|)
expr_stmt|;
block|}
DECL|method|types
annotation|@
name|Override
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|DateHistogramFacet
operator|.
name|TYPE
block|,
literal|"dateHistogram"
block|}
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|FacetCollector
name|parse
parameter_list|(
name|String
name|facetName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|keyField
init|=
literal|null
decl_stmt|;
name|String
name|valueField
init|=
literal|null
decl_stmt|;
name|String
name|valueScript
init|=
literal|null
decl_stmt|;
name|String
name|scriptLang
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
literal|null
decl_stmt|;
name|boolean
name|intervalSet
init|=
literal|false
decl_stmt|;
name|long
name|interval
init|=
literal|1
decl_stmt|;
name|String
name|sInterval
init|=
literal|null
decl_stmt|;
name|MutableDateTime
name|dateTime
init|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|DateHistogramFacet
operator|.
name|ComparatorType
name|comparatorType
init|=
name|DateHistogramFacet
operator|.
name|ComparatorType
operator|.
name|TIME
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
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
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"params"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|params
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"field"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|keyField
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
literal|"key_field"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"keyField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|keyField
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
literal|"value_field"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"valueField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|valueField
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
literal|"interval"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|intervalSet
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
name|interval
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|sInterval
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"time_zone"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"timeZone"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
name|dateTime
operator|.
name|setZone
argument_list|(
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|text
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|text
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// format like -02:30
name|dateTime
operator|.
name|setZone
argument_list|(
name|DateTimeZone
operator|.
name|forOffsetHoursMinutes
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|text
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
argument_list|)
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|text
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// id, listed here: http://joda-time.sourceforge.net/timezones.html
name|dateTime
operator|.
name|setZone
argument_list|(
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|text
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
literal|"value_script"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"valueScript"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|valueScript
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
literal|"order"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"comparator"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|comparatorType
operator|=
name|DateHistogramFacet
operator|.
name|ComparatorType
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lang"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|scriptLang
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|keyField
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"key field is required to be set for histogram facet, either using [field] or using [key_field]"
argument_list|)
throw|;
block|}
name|FieldMapper
name|mapper
init|=
name|context
operator|.
name|smartNameFieldMapper
argument_list|(
name|keyField
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"(key) field ["
operator|+
name|keyField
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
if|if
condition|(
name|mapper
operator|.
name|fieldDataType
argument_list|()
operator|!=
name|FieldDataType
operator|.
name|DefaultTypes
operator|.
name|LONG
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"(key) field ["
operator|+
name|keyField
operator|+
literal|"] is not of type date"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|intervalSet
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"[interval] is required to be set for histogram facet"
argument_list|)
throw|;
block|}
comment|// we set the rounding after we set the zone, for it to take affect
if|if
condition|(
name|sInterval
operator|!=
literal|null
condition|)
block|{
name|int
name|index
init|=
name|sInterval
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// set with rounding
name|DateFieldParser
name|fieldParser
init|=
name|dateFieldParsers
operator|.
name|get
argument_list|(
name|sInterval
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldParser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"failed to parse interval ["
operator|+
name|sInterval
operator|+
literal|"] with custom rounding using built in intervals (year/month/...)"
argument_list|)
throw|;
block|}
name|DateTimeField
name|field
init|=
name|fieldParser
operator|.
name|parse
argument_list|(
name|dateTime
operator|.
name|getChronology
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|rounding
init|=
name|this
operator|.
name|rounding
operator|.
name|get
argument_list|(
name|sInterval
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|rounding
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"failed to parse interval ["
operator|+
name|sInterval
operator|+
literal|"], rounding type ["
operator|+
operator|(
name|sInterval
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
operator|)
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
name|dateTime
operator|.
name|setRounding
argument_list|(
name|field
argument_list|,
name|rounding
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DateFieldParser
name|fieldParser
init|=
name|dateFieldParsers
operator|.
name|get
argument_list|(
name|sInterval
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldParser
operator|!=
literal|null
condition|)
block|{
name|DateTimeField
name|field
init|=
name|fieldParser
operator|.
name|parse
argument_list|(
name|dateTime
operator|.
name|getChronology
argument_list|()
argument_list|)
decl_stmt|;
name|dateTime
operator|.
name|setRounding
argument_list|(
name|field
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// time interval
try|try
block|{
name|interval
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|sInterval
argument_list|,
literal|null
argument_list|)
operator|.
name|millis
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"failed to parse interval ["
operator|+
name|sInterval
operator|+
literal|"], tried both as built in intervals (year/month/...) and as a time format"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|valueScript
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|ValueScriptDateHistogramFacetCollector
argument_list|(
name|facetName
argument_list|,
name|keyField
argument_list|,
name|scriptLang
argument_list|,
name|valueScript
argument_list|,
name|params
argument_list|,
name|dateTime
argument_list|,
name|interval
argument_list|,
name|comparatorType
argument_list|,
name|context
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|valueField
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|CountDateHistogramFacetCollector
argument_list|(
name|facetName
argument_list|,
name|keyField
argument_list|,
name|dateTime
argument_list|,
name|interval
argument_list|,
name|comparatorType
argument_list|,
name|context
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ValueDateHistogramFacetCollector
argument_list|(
name|facetName
argument_list|,
name|keyField
argument_list|,
name|valueField
argument_list|,
name|dateTime
argument_list|,
name|interval
argument_list|,
name|comparatorType
argument_list|,
name|context
argument_list|)
return|;
block|}
block|}
DECL|method|reduce
annotation|@
name|Override
specifier|public
name|Facet
name|reduce
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
parameter_list|)
block|{
name|InternalDateHistogramFacet
name|first
init|=
operator|(
name|InternalDateHistogramFacet
operator|)
name|facets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|first
operator|.
name|reduce
argument_list|(
name|name
argument_list|,
name|facets
argument_list|)
return|;
block|}
DECL|interface|DateFieldParser
specifier|static
interface|interface
name|DateFieldParser
block|{
DECL|method|parse
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
function_decl|;
DECL|class|WeekOfWeekyear
specifier|static
class|class
name|WeekOfWeekyear
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|weekOfWeekyear
argument_list|()
return|;
block|}
block|}
DECL|class|YearOfCentury
specifier|static
class|class
name|YearOfCentury
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|yearOfCentury
argument_list|()
return|;
block|}
block|}
DECL|class|MonthOfYear
specifier|static
class|class
name|MonthOfYear
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|monthOfYear
argument_list|()
return|;
block|}
block|}
DECL|class|DayOfMonth
specifier|static
class|class
name|DayOfMonth
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|dayOfMonth
argument_list|()
return|;
block|}
block|}
DECL|class|HourOfDay
specifier|static
class|class
name|HourOfDay
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|hourOfDay
argument_list|()
return|;
block|}
block|}
DECL|class|MinuteOfHour
specifier|static
class|class
name|MinuteOfHour
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|minuteOfHour
argument_list|()
return|;
block|}
block|}
DECL|class|SecondOfMinute
specifier|static
class|class
name|SecondOfMinute
implements|implements
name|DateFieldParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DateTimeField
name|parse
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|secondOfMinute
argument_list|()
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

