begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.functionscore.gauss
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
operator|.
name|gauss
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
name|Explanation
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
name|bytes
operator|.
name|BytesReference
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
name|DecayFunction
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
name|DecayFunctionBuilder
import|;
end_import

begin_class
DECL|class|GaussDecayFunctionBuilder
specifier|public
class|class
name|GaussDecayFunctionBuilder
extends|extends
name|DecayFunctionBuilder
argument_list|<
name|GaussDecayFunctionBuilder
argument_list|>
block|{
DECL|field|GAUSS_DECAY_FUNCTION
specifier|public
specifier|static
specifier|final
name|DecayFunction
name|GAUSS_DECAY_FUNCTION
init|=
operator|new
name|GaussScoreFunction
argument_list|()
decl_stmt|;
DECL|method|GaussDecayFunctionBuilder
specifier|public
name|GaussDecayFunctionBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|origin
parameter_list|,
name|Object
name|scale
parameter_list|,
name|Object
name|offset
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|origin
argument_list|,
name|scale
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
DECL|method|GaussDecayFunctionBuilder
specifier|public
name|GaussDecayFunctionBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|origin
parameter_list|,
name|Object
name|scale
parameter_list|,
name|Object
name|offset
parameter_list|,
name|double
name|decay
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|origin
argument_list|,
name|scale
argument_list|,
name|offset
argument_list|,
name|decay
argument_list|)
expr_stmt|;
block|}
DECL|method|GaussDecayFunctionBuilder
specifier|private
name|GaussDecayFunctionBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BytesReference
name|functionBytes
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|functionBytes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createFunctionBuilder
specifier|protected
name|GaussDecayFunctionBuilder
name|createFunctionBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BytesReference
name|functionBytes
parameter_list|)
block|{
return|return
operator|new
name|GaussDecayFunctionBuilder
argument_list|(
name|fieldName
argument_list|,
name|functionBytes
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|GaussDecayFunctionParser
operator|.
name|NAMES
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|getDecayFunction
specifier|public
name|DecayFunction
name|getDecayFunction
parameter_list|()
block|{
return|return
name|GAUSS_DECAY_FUNCTION
return|;
block|}
DECL|class|GaussScoreFunction
specifier|private
specifier|static
specifier|final
class|class
name|GaussScoreFunction
implements|implements
name|DecayFunction
block|{
annotation|@
name|Override
DECL|method|evaluate
specifier|public
name|double
name|evaluate
parameter_list|(
name|double
name|value
parameter_list|,
name|double
name|scale
parameter_list|)
block|{
comment|// note that we already computed scale^2 in processScale() so we do
comment|// not need to square it here.
return|return
name|Math
operator|.
name|exp
argument_list|(
literal|0.5
operator|*
name|Math
operator|.
name|pow
argument_list|(
name|value
argument_list|,
literal|2.0
argument_list|)
operator|/
name|scale
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|explainFunction
specifier|public
name|Explanation
name|explainFunction
parameter_list|(
name|String
name|valueExpl
parameter_list|,
name|double
name|value
parameter_list|,
name|double
name|scale
parameter_list|)
block|{
return|return
name|Explanation
operator|.
name|match
argument_list|(
operator|(
name|float
operator|)
name|evaluate
argument_list|(
name|value
argument_list|,
name|scale
argument_list|)
argument_list|,
literal|"exp(-0.5*pow("
operator|+
name|valueExpl
operator|+
literal|",2.0)/"
operator|+
operator|-
literal|1
operator|*
name|scale
operator|+
literal|")"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|processScale
specifier|public
name|double
name|processScale
parameter_list|(
name|double
name|scale
parameter_list|,
name|double
name|decay
parameter_list|)
block|{
return|return
literal|0.5
operator|*
name|Math
operator|.
name|pow
argument_list|(
name|scale
argument_list|,
literal|2.0
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
name|decay
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|obj
operator|!=
literal|null
operator|&&
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

