begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search.function
package|package
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
name|Explanation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|fielddata
operator|.
name|SortedNumericDoubleValues
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
comment|/**  * A function_score function that multiplies the score with the value of a  * field from the document, optionally multiplying the field by a factor first,  * and applying a modification (log, ln, sqrt, square, etc) afterwards.  */
end_comment

begin_class
DECL|class|FieldValueFactorFunction
specifier|public
class|class
name|FieldValueFactorFunction
extends|extends
name|ScoreFunction
block|{
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|boostFactor
specifier|private
specifier|final
name|float
name|boostFactor
decl_stmt|;
DECL|field|modifier
specifier|private
specifier|final
name|Modifier
name|modifier
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexNumericFieldData
name|indexFieldData
decl_stmt|;
DECL|field|values
specifier|private
name|SortedNumericDoubleValues
name|values
decl_stmt|;
DECL|method|FieldValueFactorFunction
specifier|public
name|FieldValueFactorFunction
parameter_list|(
name|String
name|field
parameter_list|,
name|float
name|boostFactor
parameter_list|,
name|Modifier
name|modifierType
parameter_list|,
name|IndexNumericFieldData
name|indexFieldData
parameter_list|)
block|{
name|super
argument_list|(
name|CombineFunction
operator|.
name|MULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|boostFactor
operator|=
name|boostFactor
expr_stmt|;
name|this
operator|.
name|modifier
operator|=
name|modifierType
expr_stmt|;
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
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
name|this
operator|.
name|values
operator|=
name|this
operator|.
name|indexFieldData
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
name|this
operator|.
name|values
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numValues
init|=
name|this
operator|.
name|values
operator|.
name|count
argument_list|()
decl_stmt|;
if|if
condition|(
name|numValues
operator|>
literal|0
condition|)
block|{
name|double
name|val
init|=
name|this
operator|.
name|values
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|*
name|boostFactor
decl_stmt|;
name|double
name|result
init|=
name|modifier
operator|.
name|apply
argument_list|(
name|val
argument_list|)
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|isNaN
argument_list|(
name|result
argument_list|)
operator|||
name|Double
operator|.
name|isInfinite
argument_list|(
name|result
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Result of field modification ["
operator|+
name|modifier
operator|.
name|toString
argument_list|()
operator|+
literal|"("
operator|+
name|val
operator|+
literal|")] must be a number"
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Missing value for field ["
operator|+
name|field
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
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
name|float
name|subQueryScore
parameter_list|)
block|{
name|Explanation
name|exp
init|=
operator|new
name|Explanation
argument_list|()
decl_stmt|;
name|String
name|modifierStr
init|=
name|modifier
operator|!=
literal|null
condition|?
name|modifier
operator|.
name|toString
argument_list|()
else|:
literal|""
decl_stmt|;
name|double
name|score
init|=
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
decl_stmt|;
name|exp
operator|.
name|setValue
argument_list|(
name|CombineFunction
operator|.
name|toFloat
argument_list|(
name|score
argument_list|)
argument_list|)
expr_stmt|;
name|exp
operator|.
name|setDescription
argument_list|(
literal|"field value function: "
operator|+
name|modifierStr
operator|+
literal|"("
operator|+
literal|"doc['"
operator|+
name|field
operator|+
literal|"'].value * factor="
operator|+
name|boostFactor
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
name|exp
return|;
block|}
comment|/**      * The Type class encapsulates the modification types that can be applied      * to the score/value product.      */
DECL|enum|Modifier
specifier|public
enum|enum
name|Modifier
block|{
DECL|enum constant|NONE
name|NONE
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|n
return|;
block|}
block|}
block|,
DECL|enum constant|LOG
name|LOG
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log10
argument_list|(
name|n
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|LOG1P
name|LOG1P
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log10
argument_list|(
name|n
operator|+
literal|1
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|LOG2P
name|LOG2P
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log10
argument_list|(
name|n
operator|+
literal|2
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|LN
name|LN
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log
argument_list|(
name|n
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|LN1P
name|LN1P
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log1p
argument_list|(
name|n
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|LN2P
name|LN2P
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log1p
argument_list|(
name|n
operator|+
literal|1
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|SQUARE
name|SQUARE
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|pow
argument_list|(
name|n
argument_list|,
literal|2
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|SQRT
name|SQRT
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
name|Math
operator|.
name|sqrt
argument_list|(
name|n
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|RECIPROCAL
name|RECIPROCAL
block|{
annotation|@
name|Override
specifier|public
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
block|{
return|return
literal|1.0
operator|/
name|n
return|;
block|}
block|}
block|;
DECL|method|apply
specifier|public
specifier|abstract
name|double
name|apply
parameter_list|(
name|double
name|n
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|NONE
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|super
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

