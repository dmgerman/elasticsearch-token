begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|xcontent
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
name|Analyzer
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
name|index
operator|.
name|mapper
operator|.
name|FieldMapperListener
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
name|MapperParsingException
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
name|MergeMappingException
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AnalyzerMapper
specifier|public
class|class
name|AnalyzerMapper
implements|implements
name|XContentMapper
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_analyzer"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|PATH
specifier|public
specifier|static
specifier|final
name|String
name|PATH
init|=
literal|"_analyzer"
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|XContentMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|AnalyzerMapper
argument_list|>
block|{
DECL|field|field
specifier|private
name|String
name|field
init|=
name|Defaults
operator|.
name|PATH
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
name|super
argument_list|(
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|builder
operator|=
name|this
expr_stmt|;
block|}
DECL|method|field
specifier|public
name|Builder
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
annotation|@
name|Override
specifier|public
name|AnalyzerMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|AnalyzerMapper
argument_list|(
name|field
argument_list|)
return|;
block|}
block|}
comment|// for now, it is parsed directly in the document parser, need to move this internal types parsing to be done here as well...
comment|//    public static class TypeParser implements XContentMapper.TypeParser {
comment|//        @Override public XContentMapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
comment|//            AnalyzerMapper.Builder builder = analyzer();
comment|//            for (Map.Entry<String, Object> entry : node.entrySet()) {
comment|//                String fieldName = Strings.toUnderscoreCase(entry.getKey());
comment|//                Object fieldNode = entry.getValue();
comment|//                if ("path".equals(fieldName)) {
comment|//                    builder.field(fieldNode.toString());
comment|//                }
comment|//            }
comment|//            return builder;
comment|//        }
comment|//    }
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|method|AnalyzerMapper
specifier|public
name|AnalyzerMapper
parameter_list|()
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|PATH
argument_list|)
expr_stmt|;
block|}
DECL|method|AnalyzerMapper
specifier|public
name|AnalyzerMapper
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Analyzer
name|analyzer
init|=
name|context
operator|.
name|docMapper
argument_list|()
operator|.
name|mappers
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
operator|!=
literal|null
condition|)
block|{
name|String
name|value
init|=
name|context
operator|.
name|doc
argument_list|()
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|context
operator|.
name|ignoredValue
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|analyzer
operator|=
name|context
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No analyzer found for ["
operator|+
name|value
operator|+
literal|"] from path ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|analyzer
operator|=
name|context
operator|.
name|docMapper
argument_list|()
operator|.
name|mappers
argument_list|()
operator|.
name|indexAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|XContentMapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{     }
DECL|method|traverse
annotation|@
name|Override
specifier|public
name|void
name|traverse
parameter_list|(
name|FieldMapperListener
name|fieldMapperListener
parameter_list|)
block|{     }
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|PATH
argument_list|)
condition|)
block|{
return|return;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|PATH
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

