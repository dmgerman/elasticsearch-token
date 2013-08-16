begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.functionscore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|ComplexExplanation
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
name|Explanation
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
name|ElasticSearchParseException
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
name|GeoDistance
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|CombineFunction
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|ScoreFunction
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
name|fielddata
operator|.
name|DoubleValues
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
name|GeoPointValues
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
name|IndexNumericFieldData
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
name|MapperService
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
name|core
operator|.
name|NumberFieldMapper
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
name|GeoPointFieldMapper
operator|.
name|GeoStringFieldMapper
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
name|query
operator|.
name|QueryParseContext
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
name|query
operator|.
name|QueryParsingException
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
name|query
operator|.
name|functionscore
operator|.
name|gauss
operator|.
name|GaussDecayFunctionBuilder
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
name|query
operator|.
name|functionscore
operator|.
name|gauss
operator|.
name|GaussDecayFunctionParser
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

begin_comment
comment|/**  * This class provides the basic functionality needed for adding a decay  * function.  *   * This parser parses this kind of input  *   *<pre>  * {@code}  * {   *      "fieldname1" : {  *          "reference" = "someValue",   *          "scale" = "someValue"  *      }   *        * }  *</pre>  *   * "reference" here refers to the reference point and "scale" to the level of  * uncertainty you have in your reference.  *<p>  *   * For example, you might want to retrieve an event that took place around the  * 20 May 2010 somewhere near Berlin. You are mainly interested in events that  * are close to the 20 May 2010 but you are unsure about your guess, maybe it  * was a week before or after that. Your "reference" for the date field would be  * "20 May 2010" and your "scale" would be "7d".  *   * This class parses the input and creates a scoring function from the  * parameters reference and scale.  *<p>  * To write a new scoring function, create a new class that inherits from this  * one and implement the getDistanceFuntion(). Furthermore, to create a builder,  * override the getName() in {@link DecayFunctionBuilder}.  *<p>  * See {@link GaussDecayFunctionBuilder} and {@link GaussDecayFunctionParser}  * for an example. The parser furthermore needs to be registered in the  * {@link org.elasticsearch.index.query.functionscore.FunctionScoreModule  * FunctionScoreModule}.  *   * **/
end_comment

begin_class
DECL|class|DecayFunctionParser
specifier|public
specifier|abstract
class|class
name|DecayFunctionParser
implements|implements
name|ScoreFunctionParser
block|{
comment|/**      * Override this function if you want to produce your own scorer.      * */
DECL|method|getDecayFunction
specifier|public
specifier|abstract
name|DecayFunction
name|getDecayFunction
parameter_list|()
function_decl|;
comment|/**      * Parses bodies of the kind      *       *<pre>      * {@code}      * {       *      "fieldname1" : {      *          "reference" = "someValue",       *          "scale" = "someValue"      *      }       *            * }      *</pre>      *       * */
annotation|@
name|Override
DECL|method|parse
specifier|public
name|ScoreFunction
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|ScoreFunction
name|scoreFunction
init|=
literal|null
decl_stmt|;
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
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
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
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
comment|// parse per field the reference and scale value
name|scoreFunction
operator|=
name|parseVariable
argument_list|(
name|currentFieldName
argument_list|,
name|parser
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Malformed score function score parameters."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Malformed score function score parameters."
argument_list|)
throw|;
block|}
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|scoreFunction
return|;
block|}
comment|// parses reference and scale parameter for field "fieldName"
DECL|method|parseVariable
specifier|private
name|ScoreFunction
name|parseVariable
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
comment|// now, the field must exist, else we cannot read the value for
comment|// the doc later
name|MapperService
operator|.
name|SmartNameFieldMappers
name|smartMappers
init|=
name|parseContext
operator|.
name|smartFieldMappers
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|smartMappers
operator|==
literal|null
operator|||
operator|!
name|smartMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"Unknown field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|smartMappers
operator|.
name|fieldMappers
argument_list|()
operator|.
name|mapper
argument_list|()
decl_stmt|;
comment|// dates and time need special handling
if|if
condition|(
name|mapper
operator|instanceof
name|DateFieldMapper
condition|)
block|{
return|return
name|parseDateVariable
argument_list|(
name|fieldName
argument_list|,
name|parser
argument_list|,
name|parseContext
argument_list|,
operator|(
name|DateFieldMapper
operator|)
name|mapper
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|mapper
operator|instanceof
name|GeoStringFieldMapper
condition|)
block|{
return|return
name|parseGeoVariable
argument_list|(
name|fieldName
argument_list|,
name|parser
argument_list|,
name|parseContext
argument_list|,
operator|(
name|GeoStringFieldMapper
operator|)
name|mapper
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|mapper
operator|instanceof
name|NumberFieldMapper
argument_list|<
name|?
argument_list|>
condition|)
block|{
return|return
name|parseNumberVariable
argument_list|(
name|fieldName
argument_list|,
name|parser
argument_list|,
name|parseContext
argument_list|,
operator|(
name|NumberFieldMapper
argument_list|<
name|?
argument_list|>
operator|)
name|mapper
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"Field "
operator|+
name|fieldName
operator|+
literal|" is of type "
operator|+
name|mapper
operator|.
name|fieldType
argument_list|()
operator|+
literal|", but only numeric types are supported."
argument_list|)
throw|;
block|}
block|}
DECL|method|parseNumberVariable
specifier|private
name|ScoreFunction
name|parseNumberVariable
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|parseContext
parameter_list|,
name|NumberFieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|parameterName
init|=
literal|null
decl_stmt|;
name|double
name|scale
init|=
literal|0
decl_stmt|;
name|double
name|reference
init|=
literal|0
decl_stmt|;
name|double
name|scaleWeight
init|=
literal|0.5
decl_stmt|;
name|double
name|offset
init|=
literal|0.0d
decl_stmt|;
name|boolean
name|scaleFound
init|=
literal|false
decl_stmt|;
name|boolean
name|refFound
init|=
literal|false
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
name|parameterName
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
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE
argument_list|)
condition|)
block|{
name|scale
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
name|scaleFound
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE_WEIGHT
argument_list|)
condition|)
block|{
name|scaleWeight
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|REFERNECE
argument_list|)
condition|)
block|{
name|reference
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
name|refFound
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|OFFSET
argument_list|)
condition|)
block|{
name|offset
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Parameter "
operator|+
name|parameterName
operator|+
literal|" not supported!"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|scaleFound
operator|||
operator|!
name|refFound
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Both "
operator|+
name|DecayFunctionBuilder
operator|.
name|SCALE
operator|+
literal|"and "
operator|+
name|DecayFunctionBuilder
operator|.
name|REFERNECE
operator|+
literal|" must be set for numeric fields."
argument_list|)
throw|;
block|}
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|numericFieldData
init|=
name|parseContext
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
return|return
operator|new
name|NumericFieldDataScoreFunction
argument_list|(
name|reference
argument_list|,
name|scale
argument_list|,
name|scaleWeight
argument_list|,
name|offset
argument_list|,
name|getDecayFunction
argument_list|()
argument_list|,
name|numericFieldData
argument_list|)
return|;
block|}
DECL|method|parseGeoVariable
specifier|private
name|ScoreFunction
name|parseGeoVariable
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|parseContext
parameter_list|,
name|GeoStringFieldMapper
name|mapper
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|parameterName
init|=
literal|null
decl_stmt|;
name|GeoPoint
name|reference
init|=
operator|new
name|GeoPoint
argument_list|()
decl_stmt|;
name|String
name|scaleString
init|=
literal|"1km"
decl_stmt|;
name|String
name|offsetString
init|=
literal|"0km"
decl_stmt|;
name|double
name|scaleWeight
init|=
literal|0.5
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
name|parameterName
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
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE
argument_list|)
condition|)
block|{
name|scaleString
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
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|REFERNECE
argument_list|)
condition|)
block|{
name|reference
operator|=
name|GeoPoint
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE_WEIGHT
argument_list|)
condition|)
block|{
name|scaleWeight
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|OFFSET
argument_list|)
condition|)
block|{
name|offsetString
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Parameter "
operator|+
name|parameterName
operator|+
literal|" not supported!"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|reference
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
name|DecayFunctionBuilder
operator|.
name|REFERNECE
operator|+
literal|"must be set for geo fields."
argument_list|)
throw|;
block|}
name|double
name|scale
init|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
name|scaleString
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
decl_stmt|;
name|double
name|offset
init|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
name|offsetString
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
decl_stmt|;
name|IndexGeoPointFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|parseContext
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
return|return
operator|new
name|GeoFieldDataScoreFunction
argument_list|(
name|reference
argument_list|,
name|scale
argument_list|,
name|scaleWeight
argument_list|,
name|offset
argument_list|,
name|getDecayFunction
argument_list|()
argument_list|,
name|indexFieldData
argument_list|)
return|;
block|}
DECL|method|parseDateVariable
specifier|private
name|ScoreFunction
name|parseDateVariable
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|parseContext
parameter_list|,
name|DateFieldMapper
name|dateFieldMapper
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|parameterName
init|=
literal|null
decl_stmt|;
name|String
name|scaleString
init|=
literal|null
decl_stmt|;
name|String
name|referenceString
init|=
literal|null
decl_stmt|;
name|String
name|offsetString
init|=
literal|"0d"
decl_stmt|;
name|double
name|scaleWeight
init|=
literal|0.5
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
name|parameterName
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
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE
argument_list|)
condition|)
block|{
name|scaleString
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
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|REFERNECE
argument_list|)
condition|)
block|{
name|referenceString
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
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE_WEIGHT
argument_list|)
condition|)
block|{
name|scaleWeight
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterName
operator|.
name|equals
argument_list|(
name|DecayFunctionBuilder
operator|.
name|OFFSET
argument_list|)
condition|)
block|{
name|offsetString
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Parameter "
operator|+
name|parameterName
operator|+
literal|" not supported!"
argument_list|)
throw|;
block|}
block|}
name|long
name|reference
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|nowInMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|referenceString
operator|!=
literal|null
condition|)
block|{
name|reference
operator|=
name|dateFieldMapper
operator|.
name|value
argument_list|(
name|referenceString
argument_list|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|scaleString
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
name|DecayFunctionBuilder
operator|.
name|SCALE
operator|+
literal|"must be set for geo fields."
argument_list|)
throw|;
block|}
name|TimeValue
name|val
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|scaleString
argument_list|,
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|24
argument_list|)
argument_list|)
decl_stmt|;
name|double
name|scale
init|=
name|val
operator|.
name|getMillis
argument_list|()
decl_stmt|;
name|val
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|offsetString
argument_list|,
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|24
argument_list|)
argument_list|)
expr_stmt|;
name|double
name|offset
init|=
name|val
operator|.
name|getMillis
argument_list|()
decl_stmt|;
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|numericFieldData
init|=
name|parseContext
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|dateFieldMapper
argument_list|)
decl_stmt|;
return|return
operator|new
name|NumericFieldDataScoreFunction
argument_list|(
name|reference
argument_list|,
name|scale
argument_list|,
name|scaleWeight
argument_list|,
name|offset
argument_list|,
name|getDecayFunction
argument_list|()
argument_list|,
name|numericFieldData
argument_list|)
return|;
block|}
DECL|class|GeoFieldDataScoreFunction
specifier|static
class|class
name|GeoFieldDataScoreFunction
extends|extends
name|AbstractDistanceScoreFunction
block|{
DECL|field|reference
specifier|private
specifier|final
name|GeoPoint
name|reference
decl_stmt|;
DECL|field|fieldData
specifier|private
specifier|final
name|IndexGeoPointFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
decl_stmt|;
DECL|field|geoPointValues
specifier|private
name|GeoPointValues
name|geoPointValues
init|=
literal|null
decl_stmt|;
DECL|field|distFunction
specifier|private
specifier|static
specifier|final
name|GeoDistance
name|distFunction
init|=
name|GeoDistance
operator|.
name|fromString
argument_list|(
literal|"arc"
argument_list|)
decl_stmt|;
DECL|method|GeoFieldDataScoreFunction
specifier|public
name|GeoFieldDataScoreFunction
parameter_list|(
name|GeoPoint
name|reference
parameter_list|,
name|double
name|scale
parameter_list|,
name|double
name|scaleWeight
parameter_list|,
name|double
name|offset
parameter_list|,
name|DecayFunction
name|func
parameter_list|,
name|IndexGeoPointFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
parameter_list|)
block|{
name|super
argument_list|(
name|scale
argument_list|,
name|scaleWeight
argument_list|,
name|offset
argument_list|,
name|func
argument_list|)
expr_stmt|;
name|this
operator|.
name|reference
operator|=
name|reference
expr_stmt|;
name|this
operator|.
name|fieldData
operator|=
name|fieldData
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
block|{
name|geoPointValues
operator|=
name|fieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getGeoPointValues
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|distance
specifier|protected
name|double
name|distance
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|GeoPoint
name|other
init|=
name|geoPointValues
operator|.
name|getValueMissing
argument_list|(
name|docId
argument_list|,
name|reference
argument_list|)
decl_stmt|;
name|double
name|distance
init|=
name|Math
operator|.
name|abs
argument_list|(
name|distFunction
operator|.
name|calculate
argument_list|(
name|reference
operator|.
name|lat
argument_list|()
argument_list|,
name|reference
operator|.
name|lon
argument_list|()
argument_list|,
name|other
operator|.
name|lat
argument_list|()
argument_list|,
name|other
operator|.
name|lon
argument_list|()
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
argument_list|)
operator|-
name|offset
decl_stmt|;
if|if
condition|(
name|distance
operator|<
literal|0.0d
condition|)
block|{
name|distance
operator|=
literal|0.0d
expr_stmt|;
block|}
return|return
name|distance
return|;
block|}
annotation|@
name|Override
DECL|method|getDistanceString
specifier|protected
name|String
name|getDistanceString
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
specifier|final
name|GeoPoint
name|other
init|=
name|geoPointValues
operator|.
name|getValueMissing
argument_list|(
name|docId
argument_list|,
name|reference
argument_list|)
decl_stmt|;
return|return
literal|"arcDistance("
operator|+
name|other
operator|+
literal|"(=doc value), "
operator|+
name|reference
operator|+
literal|"(=reference)) - "
operator|+
name|offset
operator|+
literal|"(=offset)< 0.0 ? 0.0: arcDistance("
operator|+
name|other
operator|+
literal|"(=doc value), "
operator|+
name|reference
operator|+
literal|"(=reference)) - "
operator|+
name|offset
operator|+
literal|"(=offset)"
return|;
block|}
annotation|@
name|Override
DECL|method|getFieldName
specifier|protected
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|fullName
argument_list|()
return|;
block|}
block|}
DECL|class|NumericFieldDataScoreFunction
specifier|static
class|class
name|NumericFieldDataScoreFunction
extends|extends
name|AbstractDistanceScoreFunction
block|{
DECL|field|fieldData
specifier|private
specifier|final
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
decl_stmt|;
DECL|field|reference
specifier|private
specifier|final
name|double
name|reference
decl_stmt|;
DECL|field|doubleValues
specifier|private
name|DoubleValues
name|doubleValues
decl_stmt|;
DECL|method|NumericFieldDataScoreFunction
specifier|public
name|NumericFieldDataScoreFunction
parameter_list|(
name|double
name|reference
parameter_list|,
name|double
name|scale
parameter_list|,
name|double
name|scaleWeight
parameter_list|,
name|double
name|offset
parameter_list|,
name|DecayFunction
name|func
parameter_list|,
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
parameter_list|)
block|{
name|super
argument_list|(
name|scale
argument_list|,
name|scaleWeight
argument_list|,
name|offset
argument_list|,
name|func
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldData
operator|=
name|fieldData
expr_stmt|;
name|this
operator|.
name|reference
operator|=
name|reference
expr_stmt|;
block|}
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
block|{
name|this
operator|.
name|doubleValues
operator|=
name|this
operator|.
name|fieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getDoubleValues
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|distance
specifier|protected
name|double
name|distance
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|double
name|distance
init|=
name|Math
operator|.
name|abs
argument_list|(
name|doubleValues
operator|.
name|getValueMissing
argument_list|(
name|docId
argument_list|,
name|reference
argument_list|)
operator|-
name|reference
argument_list|)
operator|-
name|offset
decl_stmt|;
if|if
condition|(
name|distance
operator|<
literal|0.0
condition|)
block|{
name|distance
operator|=
literal|0.0
expr_stmt|;
block|}
return|return
name|distance
return|;
block|}
annotation|@
name|Override
DECL|method|getDistanceString
specifier|protected
name|String
name|getDistanceString
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
literal|"Math.abs("
operator|+
name|doubleValues
operator|.
name|getValueMissing
argument_list|(
name|docId
argument_list|,
name|reference
argument_list|)
operator|+
literal|"(=doc value) - "
operator|+
name|reference
operator|+
literal|"(=reference)) - "
operator|+
name|offset
operator|+
literal|"(=offset)< 0.0 ? 0.0: Math.abs("
operator|+
name|doubleValues
operator|.
name|getValueMissing
argument_list|(
name|docId
argument_list|,
name|reference
argument_list|)
operator|+
literal|"(=doc value) - "
operator|+
name|reference
operator|+
literal|") - "
operator|+
name|offset
operator|+
literal|"(=offset)"
return|;
block|}
annotation|@
name|Override
DECL|method|getFieldName
specifier|protected
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|fullName
argument_list|()
return|;
block|}
block|}
comment|/**      * This is the base class for scoring a single field.      *       * */
DECL|class|AbstractDistanceScoreFunction
specifier|public
specifier|static
specifier|abstract
class|class
name|AbstractDistanceScoreFunction
extends|extends
name|ScoreFunction
block|{
DECL|field|scale
specifier|private
specifier|final
name|double
name|scale
decl_stmt|;
DECL|field|offset
specifier|protected
specifier|final
name|double
name|offset
decl_stmt|;
DECL|field|func
specifier|private
specifier|final
name|DecayFunction
name|func
decl_stmt|;
DECL|method|AbstractDistanceScoreFunction
specifier|public
name|AbstractDistanceScoreFunction
parameter_list|(
name|double
name|userSuppiedScale
parameter_list|,
name|double
name|userSuppliedScaleWeight
parameter_list|,
name|double
name|offset
parameter_list|,
name|DecayFunction
name|func
parameter_list|)
block|{
name|super
argument_list|(
name|CombineFunction
operator|.
name|MULT
argument_list|)
expr_stmt|;
if|if
condition|(
name|userSuppiedScale
operator|<=
literal|0.0
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|NAME
operator|+
literal|" : scale must be> 0.0."
argument_list|)
throw|;
block|}
if|if
condition|(
name|userSuppliedScaleWeight
operator|<=
literal|0.0
operator|||
name|userSuppliedScaleWeight
operator|>=
literal|1.0
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|NAME
operator|+
literal|" : scale_weight must be in the range [0..1]."
argument_list|)
throw|;
block|}
name|this
operator|.
name|scale
operator|=
name|func
operator|.
name|processScale
argument_list|(
name|userSuppiedScale
argument_list|,
name|userSuppliedScaleWeight
argument_list|)
expr_stmt|;
name|this
operator|.
name|func
operator|=
name|func
expr_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0.0d
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|NAME
operator|+
literal|" : offset must be> 0.0"
argument_list|)
throw|;
block|}
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|score
specifier|public
name|double
name|score
parameter_list|(
name|int
name|docId
parameter_list|,
name|float
name|subQueryScore
parameter_list|)
block|{
name|double
name|value
init|=
name|distance
argument_list|(
name|docId
argument_list|)
decl_stmt|;
return|return
name|func
operator|.
name|evaluate
argument_list|(
name|value
argument_list|,
name|scale
argument_list|)
return|;
block|}
comment|/**          * This function computes the distance from a defined reference. Since          * the value of the document is read from the index, it cannot be          * guaranteed that the value actually exists. If it does not, we assume          * the user handles this case in the query and return 0.          * */
DECL|method|distance
specifier|protected
specifier|abstract
name|double
name|distance
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|getDistanceString
specifier|protected
specifier|abstract
name|String
name|getDistanceString
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|getFieldName
specifier|protected
specifier|abstract
name|String
name|getFieldName
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|explainScore
specifier|public
name|Explanation
name|explainScore
parameter_list|(
name|int
name|docId
parameter_list|,
name|Explanation
name|subQueryExpl
parameter_list|)
block|{
name|ComplexExplanation
name|ce
init|=
operator|new
name|ComplexExplanation
argument_list|()
decl_stmt|;
name|ce
operator|.
name|setValue
argument_list|(
name|CombineFunction
operator|.
name|toFloat
argument_list|(
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryExpl
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ce
operator|.
name|setMatch
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ce
operator|.
name|setDescription
argument_list|(
literal|"Function for field "
operator|+
name|getFieldName
argument_list|()
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|ce
operator|.
name|addDetail
argument_list|(
name|func
operator|.
name|explainFunction
argument_list|(
name|getDistanceString
argument_list|(
name|docId
argument_list|)
argument_list|,
name|distance
argument_list|(
name|docId
argument_list|)
argument_list|,
name|scale
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ce
return|;
block|}
block|}
block|}
end_class

end_unit

