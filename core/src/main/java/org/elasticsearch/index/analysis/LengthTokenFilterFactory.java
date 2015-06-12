begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
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
name|analysis
operator|.
name|TokenStream
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
name|analysis
operator|.
name|miscellaneous
operator|.
name|LengthFilter
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
name|analysis
operator|.
name|miscellaneous
operator|.
name|Lucene43LengthFilter
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
name|Version
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
name|inject
operator|.
name|assistedinject
operator|.
name|Assisted
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
name|index
operator|.
name|Index
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|LengthTokenFilterFactory
specifier|public
class|class
name|LengthTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|min
specifier|private
specifier|final
name|int
name|min
decl_stmt|;
DECL|field|max
specifier|private
specifier|final
name|int
name|max
decl_stmt|;
DECL|field|enablePositionIncrements
specifier|private
specifier|final
name|boolean
name|enablePositionIncrements
decl_stmt|;
DECL|field|ENABLE_POS_INC_KEY
specifier|private
specifier|static
specifier|final
name|String
name|ENABLE_POS_INC_KEY
init|=
literal|"enable_position_increments"
decl_stmt|;
annotation|@
name|Inject
DECL|method|LengthTokenFilterFactory
specifier|public
name|LengthTokenFilterFactory
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
annotation|@
name|Assisted
name|String
name|name
parameter_list|,
annotation|@
name|Assisted
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|min
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"min"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|max
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"max"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_4_4
argument_list|)
operator|&&
name|settings
operator|.
name|get
argument_list|(
name|ENABLE_POS_INC_KEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|ENABLE_POS_INC_KEY
operator|+
literal|" is not supported anymore. Please fix your analysis chain or use"
operator|+
literal|" an older compatibility version (<=4.3) but beware that it might cause highlighting bugs."
argument_list|)
throw|;
block|}
name|enablePositionIncrements
operator|=
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_4_4
argument_list|)
condition|?
literal|true
else|:
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|ENABLE_POS_INC_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_4_4
argument_list|)
condition|)
block|{
return|return
operator|new
name|LengthFilter
argument_list|(
name|tokenStream
argument_list|,
name|min
argument_list|,
name|max
argument_list|)
return|;
block|}
else|else
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|final
name|TokenStream
name|filter
init|=
operator|new
name|Lucene43LengthFilter
argument_list|(
name|enablePositionIncrements
argument_list|,
name|tokenStream
argument_list|,
name|min
argument_list|,
name|max
argument_list|)
decl_stmt|;
return|return
name|filter
return|;
block|}
block|}
block|}
end_class

end_unit
