begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.common.joda
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|joda
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
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
name|org
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
comment|/**  */
end_comment

begin_class
DECL|class|DateMathParser
specifier|public
class|class
name|DateMathParser
block|{
DECL|field|dateTimeFormatter
specifier|private
specifier|final
name|FormatDateTimeFormatter
name|dateTimeFormatter
decl_stmt|;
DECL|field|timeUnit
specifier|private
specifier|final
name|TimeUnit
name|timeUnit
decl_stmt|;
DECL|method|DateMathParser
specifier|public
name|DateMathParser
parameter_list|(
name|FormatDateTimeFormatter
name|dateTimeFormatter
parameter_list|,
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
name|this
operator|.
name|dateTimeFormatter
operator|=
name|dateTimeFormatter
expr_stmt|;
name|this
operator|.
name|timeUnit
operator|=
name|timeUnit
expr_stmt|;
block|}
DECL|method|parse
specifier|public
name|long
name|parse
parameter_list|(
name|String
name|text
parameter_list|,
name|long
name|now
parameter_list|)
block|{
return|return
name|parse
argument_list|(
name|text
argument_list|,
name|now
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
return|;
block|}
DECL|method|parseUpperInclusive
specifier|public
name|long
name|parseUpperInclusive
parameter_list|(
name|String
name|text
parameter_list|,
name|long
name|now
parameter_list|)
block|{
return|return
name|parse
argument_list|(
name|text
argument_list|,
name|now
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|long
name|parse
parameter_list|(
name|String
name|text
parameter_list|,
name|long
name|now
parameter_list|,
name|boolean
name|roundUp
parameter_list|,
name|boolean
name|upperInclusive
parameter_list|)
block|{
name|long
name|time
decl_stmt|;
name|String
name|mathString
decl_stmt|;
if|if
condition|(
name|text
operator|.
name|startsWith
argument_list|(
literal|"now"
argument_list|)
condition|)
block|{
name|time
operator|=
name|now
expr_stmt|;
name|mathString
operator|=
name|text
operator|.
name|substring
argument_list|(
literal|"now"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|index
init|=
name|text
operator|.
name|indexOf
argument_list|(
literal|"||"
argument_list|)
decl_stmt|;
name|String
name|parseString
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
name|parseString
operator|=
name|text
expr_stmt|;
name|mathString
operator|=
literal|""
expr_stmt|;
comment|// nothing else
block|}
else|else
block|{
name|parseString
operator|=
name|text
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|mathString
operator|=
name|text
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|upperInclusive
condition|)
block|{
name|time
operator|=
name|parseUpperInclusiveStringValue
argument_list|(
name|parseString
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|time
operator|=
name|parseStringValue
argument_list|(
name|parseString
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|mathString
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|time
return|;
block|}
return|return
name|parseMath
argument_list|(
name|mathString
argument_list|,
name|time
argument_list|,
name|roundUp
argument_list|)
return|;
block|}
DECL|method|parseMath
specifier|private
name|long
name|parseMath
parameter_list|(
name|String
name|mathString
parameter_list|,
name|long
name|time
parameter_list|,
name|boolean
name|roundUp
parameter_list|)
throws|throws
name|ElasticSearchParseException
block|{
name|MutableDateTime
name|dateTime
init|=
operator|new
name|MutableDateTime
argument_list|(
name|time
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|mathString
operator|.
name|length
argument_list|()
condition|;
control|)
block|{
name|char
name|c
init|=
name|mathString
operator|.
name|charAt
argument_list|(
name|i
operator|++
argument_list|)
decl_stmt|;
name|int
name|type
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'/'
condition|)
block|{
name|type
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'+'
condition|)
block|{
name|type
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'-'
condition|)
block|{
name|type
operator|=
literal|2
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"operator not supported for date math ["
operator|+
name|mathString
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|int
name|num
decl_stmt|;
if|if
condition|(
operator|!
name|Character
operator|.
name|isDigit
argument_list|(
name|mathString
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
name|num
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|int
name|numFrom
init|=
name|i
decl_stmt|;
while|while
condition|(
name|Character
operator|.
name|isDigit
argument_list|(
name|mathString
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
name|num
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|mathString
operator|.
name|substring
argument_list|(
name|numFrom
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
comment|// rounding is only allowed on whole numbers
if|if
condition|(
name|num
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"rounding `/` can only be used on single unit types ["
operator|+
name|mathString
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|char
name|unit
init|=
name|mathString
operator|.
name|charAt
argument_list|(
name|i
operator|++
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|unit
condition|)
block|{
case|case
literal|'M'
case|:
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|roundUp
condition|)
block|{
name|dateTime
operator|.
name|monthOfYear
argument_list|()
operator|.
name|roundCeiling
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|dateTime
operator|.
name|monthOfYear
argument_list|()
operator|.
name|roundFloor
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|dateTime
operator|.
name|addMonths
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|dateTime
operator|.
name|addMonths
argument_list|(
operator|-
name|num
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|'w'
case|:
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|roundUp
condition|)
block|{
name|dateTime
operator|.
name|weekOfWeekyear
argument_list|()
operator|.
name|roundCeiling
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|dateTime
operator|.
name|weekOfWeekyear
argument_list|()
operator|.
name|roundFloor
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|dateTime
operator|.
name|addWeeks
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|dateTime
operator|.
name|addWeeks
argument_list|(
operator|-
name|num
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|'d'
case|:
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|roundUp
condition|)
block|{
name|dateTime
operator|.
name|dayOfMonth
argument_list|()
operator|.
name|roundCeiling
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|dateTime
operator|.
name|dayOfMonth
argument_list|()
operator|.
name|roundFloor
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|dateTime
operator|.
name|addDays
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|dateTime
operator|.
name|addDays
argument_list|(
operator|-
name|num
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|'h'
case|:
case|case
literal|'H'
case|:
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|roundUp
condition|)
block|{
name|dateTime
operator|.
name|hourOfDay
argument_list|()
operator|.
name|roundCeiling
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|dateTime
operator|.
name|hourOfDay
argument_list|()
operator|.
name|roundFloor
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|dateTime
operator|.
name|addHours
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|dateTime
operator|.
name|addHours
argument_list|(
operator|-
name|num
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|'m'
case|:
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|roundUp
condition|)
block|{
name|dateTime
operator|.
name|minuteOfHour
argument_list|()
operator|.
name|roundCeiling
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|dateTime
operator|.
name|minuteOfHour
argument_list|()
operator|.
name|roundFloor
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|dateTime
operator|.
name|addMinutes
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|dateTime
operator|.
name|addMinutes
argument_list|(
operator|-
name|num
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|'s'
case|:
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|roundUp
condition|)
block|{
name|dateTime
operator|.
name|secondOfMinute
argument_list|()
operator|.
name|roundCeiling
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|dateTime
operator|.
name|secondOfMinute
argument_list|()
operator|.
name|roundFloor
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|dateTime
operator|.
name|addSeconds
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|dateTime
operator|.
name|addSeconds
argument_list|(
operator|-
name|num
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"unit ["
operator|+
name|unit
operator|+
literal|"] not supported for date math ["
operator|+
name|mathString
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ElasticSearchParseException
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchParseException
operator|)
name|e
throw|;
block|}
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"failed to parse date math ["
operator|+
name|mathString
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|dateTime
operator|.
name|getMillis
argument_list|()
return|;
block|}
DECL|method|parseStringValue
specifier|private
name|long
name|parseStringValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
return|return
name|dateTimeFormatter
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
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
try|try
block|{
name|long
name|time
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|timeUnit
operator|.
name|toMillis
argument_list|(
name|time
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"failed to parse date field ["
operator|+
name|value
operator|+
literal|"], tried both date format ["
operator|+
name|dateTimeFormatter
operator|.
name|format
argument_list|()
operator|+
literal|"], and timestamp number"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|parseUpperInclusiveStringValue
specifier|private
name|long
name|parseUpperInclusiveStringValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
comment|// we create a date time for inclusive upper range, we "include" by default the day level data
comment|// so something like 2011-01-01 will include the full first day of 2011.
comment|// we also use 1970-01-01 as the base for it so we can handle searches like 10:12:55 (just time)
comment|// since when we index those, the base is 1970-01-01
name|MutableDateTime
name|dateTime
init|=
operator|new
name|MutableDateTime
argument_list|(
literal|1970
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|23
argument_list|,
literal|59
argument_list|,
literal|59
argument_list|,
literal|999
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|int
name|location
init|=
name|dateTimeFormatter
operator|.
name|parser
argument_list|()
operator|.
name|parseInto
argument_list|(
name|dateTime
argument_list|,
name|value
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// if we parsed all the string value, we are good
if|if
condition|(
name|location
operator|==
name|value
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
name|dateTime
operator|.
name|getMillis
argument_list|()
return|;
block|}
comment|// if we did not manage to parse, or the year is really high year which is unreasonable
comment|// see if its a number
if|if
condition|(
name|location
operator|<=
literal|0
operator|||
name|dateTime
operator|.
name|getYear
argument_list|()
operator|>
literal|5000
condition|)
block|{
try|try
block|{
name|long
name|time
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|timeUnit
operator|.
name|toMillis
argument_list|(
name|time
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"failed to parse date field ["
operator|+
name|value
operator|+
literal|"], tried both date format ["
operator|+
name|dateTimeFormatter
operator|.
name|format
argument_list|()
operator|+
literal|"], and timestamp number"
argument_list|)
throw|;
block|}
block|}
return|return
name|dateTime
operator|.
name|getMillis
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
try|try
block|{
name|long
name|time
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|timeUnit
operator|.
name|toMillis
argument_list|(
name|time
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"failed to parse date field ["
operator|+
name|value
operator|+
literal|"], tried both date format ["
operator|+
name|dateTimeFormatter
operator|.
name|format
argument_list|()
operator|+
literal|"], and timestamp number"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

