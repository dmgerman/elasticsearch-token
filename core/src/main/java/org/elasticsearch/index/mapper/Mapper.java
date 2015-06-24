begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|elasticsearch
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
name|Nullable
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
name|Strings
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
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
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
name|similarity
operator|.
name|SimilarityLookupService
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

begin_interface
DECL|interface|Mapper
specifier|public
interface|interface
name|Mapper
extends|extends
name|ToXContent
extends|,
name|Iterable
argument_list|<
name|Mapper
argument_list|>
block|{
DECL|field|EMPTY_ARRAY
name|Mapper
index|[]
name|EMPTY_ARRAY
init|=
operator|new
name|Mapper
index|[
literal|0
index|]
decl_stmt|;
DECL|class|BuilderContext
class|class
name|BuilderContext
block|{
DECL|field|indexSettings
specifier|private
specifier|final
name|Settings
name|indexSettings
decl_stmt|;
DECL|field|contentPath
specifier|private
specifier|final
name|ContentPath
name|contentPath
decl_stmt|;
DECL|method|BuilderContext
specifier|public
name|BuilderContext
parameter_list|(
name|Settings
name|indexSettings
parameter_list|,
name|ContentPath
name|contentPath
parameter_list|)
block|{
name|this
operator|.
name|contentPath
operator|=
name|contentPath
expr_stmt|;
name|this
operator|.
name|indexSettings
operator|=
name|indexSettings
expr_stmt|;
block|}
DECL|method|path
specifier|public
name|ContentPath
name|path
parameter_list|()
block|{
return|return
name|this
operator|.
name|contentPath
return|;
block|}
annotation|@
name|Nullable
DECL|method|indexSettings
specifier|public
name|Settings
name|indexSettings
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexSettings
return|;
block|}
annotation|@
name|Nullable
DECL|method|indexCreatedVersion
specifier|public
name|Version
name|indexCreatedVersion
parameter_list|()
block|{
if|if
condition|(
name|indexSettings
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Version
operator|.
name|indexCreated
argument_list|(
name|indexSettings
argument_list|)
return|;
block|}
block|}
DECL|class|Builder
specifier|abstract
class|class
name|Builder
parameter_list|<
name|T
extends|extends
name|Builder
parameter_list|,
name|Y
extends|extends
name|Mapper
parameter_list|>
block|{
DECL|field|name
specifier|public
name|String
name|name
decl_stmt|;
DECL|field|builder
specifier|protected
name|T
name|builder
decl_stmt|;
DECL|method|Builder
specifier|protected
name|Builder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|build
specifier|public
specifier|abstract
name|Y
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
function_decl|;
block|}
DECL|interface|TypeParser
interface|interface
name|TypeParser
block|{
DECL|class|ParserContext
class|class
name|ParserContext
block|{
DECL|field|analysisService
specifier|private
specifier|final
name|AnalysisService
name|analysisService
decl_stmt|;
DECL|field|similarityLookupService
specifier|private
specifier|final
name|SimilarityLookupService
name|similarityLookupService
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|typeParsers
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|TypeParser
argument_list|>
name|typeParsers
decl_stmt|;
DECL|field|indexVersionCreated
specifier|private
specifier|final
name|Version
name|indexVersionCreated
decl_stmt|;
DECL|method|ParserContext
specifier|public
name|ParserContext
parameter_list|(
name|AnalysisService
name|analysisService
parameter_list|,
name|SimilarityLookupService
name|similarityLookupService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|TypeParser
argument_list|>
name|typeParsers
parameter_list|,
name|Version
name|indexVersionCreated
parameter_list|)
block|{
name|this
operator|.
name|analysisService
operator|=
name|analysisService
expr_stmt|;
name|this
operator|.
name|similarityLookupService
operator|=
name|similarityLookupService
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|typeParsers
operator|=
name|typeParsers
expr_stmt|;
name|this
operator|.
name|indexVersionCreated
operator|=
name|indexVersionCreated
expr_stmt|;
block|}
DECL|method|analysisService
specifier|public
name|AnalysisService
name|analysisService
parameter_list|()
block|{
return|return
name|analysisService
return|;
block|}
DECL|method|similarityLookupService
specifier|public
name|SimilarityLookupService
name|similarityLookupService
parameter_list|()
block|{
return|return
name|similarityLookupService
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
DECL|method|typeParser
specifier|public
name|TypeParser
name|typeParser
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
name|typeParsers
operator|.
name|get
argument_list|(
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
DECL|method|indexVersionCreated
specifier|public
name|Version
name|indexVersionCreated
parameter_list|()
block|{
return|return
name|indexVersionCreated
return|;
block|}
block|}
DECL|method|parse
name|Mapper
operator|.
name|Builder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
function_decl|;
block|}
DECL|method|name
name|String
name|name
parameter_list|()
function_decl|;
DECL|method|merge
name|void
name|merge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|MergeResult
name|mergeResult
parameter_list|)
throws|throws
name|MergeMappingException
function_decl|;
block|}
end_interface

end_unit

