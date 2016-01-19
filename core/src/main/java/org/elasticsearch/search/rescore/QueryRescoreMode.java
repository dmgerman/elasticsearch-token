begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.rescore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
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
name|Writeable
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
name|Locale
import|;
end_import

begin_enum
DECL|enum|QueryRescoreMode
specifier|public
enum|enum
name|QueryRescoreMode
implements|implements
name|Writeable
argument_list|<
name|QueryRescoreMode
argument_list|>
block|{
DECL|enum constant|Avg
name|Avg
block|{
annotation|@
name|Override
specifier|public
name|float
name|combine
parameter_list|(
name|float
name|primary
parameter_list|,
name|float
name|secondary
parameter_list|)
block|{
return|return
operator|(
name|primary
operator|+
name|secondary
operator|)
operator|/
literal|2
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"avg"
return|;
block|}
block|}
block|,
DECL|enum constant|Max
name|Max
block|{
annotation|@
name|Override
specifier|public
name|float
name|combine
parameter_list|(
name|float
name|primary
parameter_list|,
name|float
name|secondary
parameter_list|)
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
name|primary
argument_list|,
name|secondary
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"max"
return|;
block|}
block|}
block|,
DECL|enum constant|Min
name|Min
block|{
annotation|@
name|Override
specifier|public
name|float
name|combine
parameter_list|(
name|float
name|primary
parameter_list|,
name|float
name|secondary
parameter_list|)
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
name|primary
argument_list|,
name|secondary
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"min"
return|;
block|}
block|}
block|,
DECL|enum constant|Total
name|Total
block|{
annotation|@
name|Override
specifier|public
name|float
name|combine
parameter_list|(
name|float
name|primary
parameter_list|,
name|float
name|secondary
parameter_list|)
block|{
return|return
name|primary
operator|+
name|secondary
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"sum"
return|;
block|}
block|}
block|,
DECL|enum constant|Multiply
name|Multiply
block|{
annotation|@
name|Override
specifier|public
name|float
name|combine
parameter_list|(
name|float
name|primary
parameter_list|,
name|float
name|secondary
parameter_list|)
block|{
return|return
name|primary
operator|*
name|secondary
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"product"
return|;
block|}
block|}
block|;
DECL|method|combine
specifier|public
specifier|abstract
name|float
name|combine
parameter_list|(
name|float
name|primary
parameter_list|,
name|float
name|secondary
parameter_list|)
function_decl|;
DECL|field|PROTOTYPE
specifier|static
name|QueryRescoreMode
name|PROTOTYPE
init|=
name|Total
decl_stmt|;
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|QueryRescoreMode
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ordinal
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|ordinal
operator|<
literal|0
operator|||
name|ordinal
operator|>=
name|values
argument_list|()
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown ScoreMode ordinal ["
operator|+
name|ordinal
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|values
argument_list|()
index|[
name|ordinal
index|]
return|;
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
name|writeVInt
argument_list|(
name|this
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|QueryRescoreMode
name|fromString
parameter_list|(
name|String
name|scoreMode
parameter_list|)
block|{
for|for
control|(
name|QueryRescoreMode
name|mode
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|scoreMode
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|equals
argument_list|(
name|mode
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|mode
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"illegal score_mode ["
operator|+
name|scoreMode
operator|+
literal|"]"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
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
end_enum

end_unit

