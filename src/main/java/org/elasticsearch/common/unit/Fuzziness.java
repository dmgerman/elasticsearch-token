begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.unit
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|FuzzyQuery
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
name|automaton
operator|.
name|LevenshteinAutomata
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
name|xcontent
operator|.
name|ToXContent
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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

begin_comment
comment|/**  * A unit class that encapsulates all in-exact search  * parsing and conversion from similarities to edit distances  * etc.  */
end_comment

begin_class
DECL|class|Fuzziness
specifier|public
specifier|final
class|class
name|Fuzziness
implements|implements
name|ToXContent
block|{
DECL|field|X_FIELD_NAME
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|X_FIELD_NAME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"fuzziness"
argument_list|)
decl_stmt|;
DECL|field|ZERO
specifier|public
specifier|static
specifier|final
name|Fuzziness
name|ZERO
init|=
operator|new
name|Fuzziness
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|ONE
specifier|public
specifier|static
specifier|final
name|Fuzziness
name|ONE
init|=
operator|new
name|Fuzziness
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|field|TWO
specifier|public
specifier|static
specifier|final
name|Fuzziness
name|TWO
init|=
operator|new
name|Fuzziness
argument_list|(
literal|2
argument_list|)
decl_stmt|;
DECL|field|AUTO
specifier|public
specifier|static
specifier|final
name|Fuzziness
name|AUTO
init|=
operator|new
name|Fuzziness
argument_list|(
literal|"AUTO"
argument_list|)
decl_stmt|;
DECL|field|FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|X_FIELD_NAME
operator|.
name|camelCase
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|fuzziness
specifier|private
specifier|final
name|Object
name|fuzziness
decl_stmt|;
DECL|method|Fuzziness
specifier|private
name|Fuzziness
parameter_list|(
name|int
name|fuzziness
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|fuzziness
operator|>=
literal|0
operator|&&
name|fuzziness
operator|<=
literal|2
argument_list|,
literal|"Valid edit distances are [0, 1, 2] but was ["
operator|+
name|fuzziness
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|this
operator|.
name|fuzziness
operator|=
name|fuzziness
expr_stmt|;
block|}
DECL|method|Fuzziness
specifier|private
name|Fuzziness
parameter_list|(
name|float
name|fuzziness
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|fuzziness
operator|>=
literal|0.0
operator|&&
name|fuzziness
operator|<
literal|1.0f
argument_list|,
literal|"Valid similarities must be in the interval [0..1] but was ["
operator|+
name|fuzziness
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|this
operator|.
name|fuzziness
operator|=
name|fuzziness
expr_stmt|;
block|}
DECL|method|Fuzziness
specifier|private
name|Fuzziness
parameter_list|(
name|String
name|fuzziness
parameter_list|)
block|{
name|this
operator|.
name|fuzziness
operator|=
name|fuzziness
expr_stmt|;
block|}
comment|/**      * Creates a {@link Fuzziness} instance from a similarity. The value must be in the range<tt>[0..1)</tt>      */
DECL|method|fromSimilarity
specifier|public
specifier|static
name|Fuzziness
name|fromSimilarity
parameter_list|(
name|float
name|similarity
parameter_list|)
block|{
return|return
operator|new
name|Fuzziness
argument_list|(
name|similarity
argument_list|)
return|;
block|}
comment|/**      * Creates a {@link Fuzziness} instance from an edit distance. The value must be one of<tt>[0, 1, 2]</tt>      */
DECL|method|fromEdits
specifier|public
specifier|static
name|Fuzziness
name|fromEdits
parameter_list|(
name|int
name|edits
parameter_list|)
block|{
return|return
operator|new
name|Fuzziness
argument_list|(
name|edits
argument_list|)
return|;
block|}
DECL|method|build
specifier|public
specifier|static
name|Fuzziness
name|build
parameter_list|(
name|Object
name|fuzziness
parameter_list|)
block|{
if|if
condition|(
name|fuzziness
operator|instanceof
name|Fuzziness
condition|)
block|{
return|return
operator|(
name|Fuzziness
operator|)
name|fuzziness
return|;
block|}
name|String
name|string
init|=
name|fuzziness
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|AUTO
operator|.
name|asString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|string
argument_list|)
condition|)
block|{
return|return
name|AUTO
return|;
block|}
return|return
operator|new
name|Fuzziness
argument_list|(
name|string
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|Fuzziness
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|token
condition|)
block|{
case|case
name|VALUE_STRING
case|:
case|case
name|VALUE_NUMBER
case|:
specifier|final
name|String
name|fuzziness
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|AUTO
operator|.
name|asString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|fuzziness
argument_list|)
condition|)
block|{
return|return
name|AUTO
return|;
block|}
try|try
block|{
specifier|final
name|int
name|minimumSimilarity
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|fuzziness
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|minimumSimilarity
condition|)
block|{
case|case
literal|0
case|:
return|return
name|ZERO
return|;
case|case
literal|1
case|:
return|return
name|ONE
return|;
case|case
literal|2
case|:
return|return
name|TWO
return|;
default|default:
return|return
name|build
argument_list|(
name|fuzziness
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
name|build
argument_list|(
name|fuzziness
argument_list|)
return|;
block|}
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse fuzziness on token: ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|toXContent
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
return|return
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|,
name|boolean
name|includeFieldName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|includeFieldName
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|X_FIELD_NAME
argument_list|,
name|fuzziness
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|value
argument_list|(
name|fuzziness
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|asDistance
specifier|public
name|int
name|asDistance
parameter_list|()
block|{
return|return
name|asDistance
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|method|asDistance
specifier|public
name|int
name|asDistance
parameter_list|(
name|String
name|text
parameter_list|)
block|{
if|if
condition|(
name|fuzziness
operator|instanceof
name|String
condition|)
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
comment|//AUTO
specifier|final
name|int
name|len
init|=
name|termLen
argument_list|(
name|text
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|<=
literal|2
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|len
operator|>
literal|5
condition|)
block|{
return|return
literal|2
return|;
block|}
else|else
block|{
return|return
literal|1
return|;
block|}
block|}
block|}
return|return
name|FuzzyQuery
operator|.
name|floatToEdits
argument_list|(
name|asFloat
argument_list|()
argument_list|,
name|termLen
argument_list|(
name|text
argument_list|)
argument_list|)
return|;
block|}
DECL|method|asTimeValue
specifier|public
name|TimeValue
name|asTimeValue
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|1
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|"fuzziness"
argument_list|)
return|;
block|}
block|}
DECL|method|asLong
specifier|public
name|long
name|asLong
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
literal|1
return|;
block|}
try|try
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
operator|(
name|long
operator|)
name|Double
operator|.
name|parseDouble
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|asInt
specifier|public
name|int
name|asInt
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
literal|1
return|;
block|}
try|try
block|{
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
operator|(
name|int
operator|)
name|Float
operator|.
name|parseFloat
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|asShort
specifier|public
name|short
name|asShort
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
literal|1
return|;
block|}
try|try
block|{
return|return
name|Short
operator|.
name|parseShort
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
operator|(
name|short
operator|)
name|Float
operator|.
name|parseFloat
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|asByte
specifier|public
name|byte
name|asByte
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
literal|1
return|;
block|}
try|try
block|{
return|return
name|Byte
operator|.
name|parseByte
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
operator|(
name|byte
operator|)
name|Float
operator|.
name|parseFloat
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|asDouble
specifier|public
name|double
name|asDouble
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
literal|1d
return|;
block|}
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|asFloat
specifier|public
name|float
name|asFloat
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
return|return
literal|1f
return|;
block|}
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|asSimilarity
specifier|public
name|float
name|asSimilarity
parameter_list|()
block|{
return|return
name|asSimilarity
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|method|asSimilarity
specifier|public
name|float
name|asSimilarity
parameter_list|(
name|String
name|text
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|AUTO
condition|)
block|{
specifier|final
name|int
name|len
init|=
name|termLen
argument_list|(
name|text
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|<=
literal|2
condition|)
block|{
return|return
literal|0.0f
return|;
block|}
elseif|else
if|if
condition|(
name|len
operator|>
literal|5
condition|)
block|{
return|return
literal|0.5f
return|;
block|}
else|else
block|{
return|return
literal|0.66f
return|;
block|}
comment|//            return dist == 0 ? dist : Math.min(0.999f, Math.max(0.0f, 1.0f - ((float) dist/ (float) termLen(text))));
block|}
if|if
condition|(
name|fuzziness
operator|instanceof
name|Float
condition|)
block|{
comment|// it's a similarity
return|return
operator|(
operator|(
name|Float
operator|)
name|fuzziness
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|fuzziness
operator|instanceof
name|Integer
condition|)
block|{
comment|// it's an edit!
name|int
name|dist
init|=
name|Math
operator|.
name|min
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|fuzziness
operator|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|LevenshteinAutomata
operator|.
name|MAXIMUM_SUPPORTED_DISTANCE
argument_list|)
decl_stmt|;
return|return
name|Math
operator|.
name|min
argument_list|(
literal|0.999f
argument_list|,
name|Math
operator|.
name|max
argument_list|(
literal|0.0f
argument_list|,
literal|1.0f
operator|-
operator|(
operator|(
name|float
operator|)
name|dist
operator|/
operator|(
name|float
operator|)
name|termLen
argument_list|(
name|text
argument_list|)
operator|)
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
specifier|final
name|float
name|similarity
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|fuzziness
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|similarity
operator|>=
literal|0.0f
operator|&&
name|similarity
operator|<
literal|1.0f
condition|)
block|{
return|return
name|similarity
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't get similarity from fuzziness ["
operator|+
name|fuzziness
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|termLen
specifier|private
name|int
name|termLen
parameter_list|(
name|String
name|text
parameter_list|)
block|{
return|return
name|text
operator|==
literal|null
condition|?
literal|5
else|:
name|text
operator|.
name|codePointCount
argument_list|(
literal|0
argument_list|,
name|text
operator|.
name|length
argument_list|()
argument_list|)
return|;
comment|// 5 avg term length in english
block|}
DECL|method|asString
specifier|public
name|String
name|asString
parameter_list|()
block|{
return|return
name|fuzziness
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

