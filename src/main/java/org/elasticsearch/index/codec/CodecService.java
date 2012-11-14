begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.codec
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|codec
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
name|collect
operator|.
name|ImmutableMap
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
name|codecs
operator|.
name|Codec
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
name|settings
operator|.
name|ImmutableSettings
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
name|AbstractIndexComponent
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatService
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|CodecService
specifier|public
class|class
name|CodecService
extends|extends
name|AbstractIndexComponent
block|{
DECL|field|postingsFormatService
specifier|private
specifier|final
name|PostingsFormatService
name|postingsFormatService
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|codecs
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Codec
argument_list|>
name|codecs
decl_stmt|;
DECL|method|CodecService
specifier|public
name|CodecService
parameter_list|(
name|Index
name|index
parameter_list|)
block|{
name|this
argument_list|(
name|index
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
DECL|method|CodecService
specifier|public
name|CodecService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
operator|new
name|PostingsFormatService
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|CodecService
specifier|public
name|CodecService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|PostingsFormatService
name|postingsFormatService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|postingsFormatService
operator|=
name|postingsFormatService
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Codec
argument_list|>
name|codecs
init|=
name|MapBuilder
operator|.
expr|<
name|String
decl_stmt|,
name|Codec
decl|>
name|newMapBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapperService
operator|==
literal|null
condition|)
block|{
name|codecs
operator|.
name|put
argument_list|(
literal|"default"
argument_list|,
name|Codec
operator|.
name|getDefault
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|codecs
operator|.
name|put
argument_list|(
literal|"default"
argument_list|,
operator|new
name|PerFieldMappingPostingFormatCodec
argument_list|(
name|mapperService
argument_list|,
name|postingsFormatService
operator|.
name|get
argument_list|(
literal|"default"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|codec
range|:
name|Codec
operator|.
name|availableCodecs
argument_list|()
control|)
block|{
name|codecs
operator|.
name|put
argument_list|(
name|codec
argument_list|,
name|Codec
operator|.
name|forName
argument_list|(
name|codec
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|codecs
operator|=
name|codecs
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
DECL|method|postingsFormatService
specifier|public
name|PostingsFormatService
name|postingsFormatService
parameter_list|()
block|{
return|return
name|this
operator|.
name|postingsFormatService
return|;
block|}
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|mapperService
return|;
block|}
DECL|method|codec
specifier|public
name|Codec
name|codec
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ElasticSearchIllegalArgumentException
block|{
name|Codec
name|codec
init|=
name|codecs
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to find codec ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|codec
return|;
block|}
block|}
end_class

end_unit

