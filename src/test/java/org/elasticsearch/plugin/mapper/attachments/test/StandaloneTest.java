begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.mapper.attachments.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|test
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
name|common
operator|.
name|cli
operator|.
name|CliTool
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
name|cli
operator|.
name|CliToolConfig
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
name|cli
operator|.
name|Terminal
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
name|cli
operator|.
name|commons
operator|.
name|CommandLine
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
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
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
name|DocumentMapper
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
name|DocumentMapperParser
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
name|ParseContext
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
name|attachment
operator|.
name|AttachmentMapper
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
name|xcontent
operator|.
name|MapperTestUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|net
operator|.
name|URL
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|cli
operator|.
name|CliToolConfig
operator|.
name|Builder
operator|.
name|cmd
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|cli
operator|.
name|CliToolConfig
operator|.
name|Builder
operator|.
name|option
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|copyToByteArray
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|copyToStringFromClasspath
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_comment
comment|/**  * This class provides a simple main class which can be used to test what is extracted from a given binary file.  * You can run it using  *  -u file://URL/TO/YOUR/DOC  *  -s set extracted size (default to mapper attachment size)  *  BASE64 encoded binary  *  * Example:  *  StandaloneTest BASE64Text  *  StandaloneTest -u /tmp/mydoc.pdf  *  StandaloneTest -u /tmp/mydoc.pdf -s 1000000  */
end_comment

begin_class
DECL|class|StandaloneTest
specifier|public
class|class
name|StandaloneTest
extends|extends
name|CliTool
block|{
DECL|field|CONFIG
specifier|private
specifier|static
specifier|final
name|CliToolConfig
name|CONFIG
init|=
name|CliToolConfig
operator|.
name|config
argument_list|(
literal|"tika"
argument_list|,
name|StandaloneTest
operator|.
name|class
argument_list|)
operator|.
name|cmds
argument_list|(
name|TikaTest
operator|.
name|CMD
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|class|TikaTest
specifier|static
class|class
name|TikaTest
extends|extends
name|Command
block|{
DECL|field|NAME
specifier|private
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"tika"
decl_stmt|;
DECL|field|url
specifier|private
specifier|final
name|String
name|url
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|Integer
name|size
decl_stmt|;
DECL|field|base64text
specifier|private
specifier|final
name|String
name|base64text
decl_stmt|;
DECL|field|docMapper
specifier|private
specifier|final
name|DocumentMapper
name|docMapper
decl_stmt|;
DECL|field|CMD
specifier|private
specifier|static
specifier|final
name|CliToolConfig
operator|.
name|Cmd
name|CMD
init|=
name|cmd
argument_list|(
name|NAME
argument_list|,
name|TikaTest
operator|.
name|class
argument_list|)
operator|.
name|options
argument_list|(
name|option
argument_list|(
literal|"u"
argument_list|,
literal|"url"
argument_list|)
operator|.
name|required
argument_list|(
literal|false
argument_list|)
operator|.
name|hasArg
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|options
argument_list|(
name|option
argument_list|(
literal|"s"
argument_list|,
literal|"size"
argument_list|)
operator|.
name|required
argument_list|(
literal|false
argument_list|)
operator|.
name|hasArg
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|method|TikaTest
specifier|protected
name|TikaTest
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|String
name|url
parameter_list|,
name|Integer
name|size
parameter_list|,
name|String
name|base64text
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|terminal
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|url
operator|=
name|url
expr_stmt|;
name|this
operator|.
name|base64text
operator|=
name|base64text
expr_stmt|;
name|DocumentMapperParser
name|mapperParser
init|=
name|MapperTestUtils
operator|.
name|newMapperParser
argument_list|()
decl_stmt|;
name|mapperParser
operator|.
name|putTypeParser
argument_list|(
name|AttachmentMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|AttachmentMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/xcontent/test-mapping.json"
argument_list|)
decl_stmt|;
name|docMapper
operator|=
name|mapperParser
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|ExitStatus
name|execute
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Environment
name|env
parameter_list|)
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"_id"
argument_list|,
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"file"
argument_list|)
operator|.
name|startObject
argument_list|()
decl_stmt|;
if|if
condition|(
name|base64text
operator|!=
literal|null
condition|)
block|{
comment|// If base64 is provided
name|builder
operator|.
name|field
argument_list|(
literal|"_content"
argument_list|,
name|base64text
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// A file is provided
name|File
name|file
init|=
operator|new
name|File
argument_list|(
operator|new
name|URL
argument_list|(
name|url
argument_list|)
operator|.
name|getFile
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|exists
init|=
name|file
operator|.
name|exists
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|exists
condition|)
block|{
return|return
name|ExitStatus
operator|.
name|IO_ERROR
return|;
block|}
name|byte
index|[]
name|bytes
init|=
name|copyToByteArray
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_content"
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|size
operator|>=
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_indexed_chars"
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|BytesReference
name|json
init|=
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|ParseContext
operator|.
name|Document
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
name|json
argument_list|)
operator|.
name|rootDoc
argument_list|()
decl_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"## Extracted text"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"--------------------- BEGIN -----------------------"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"%s"
argument_list|,
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
literal|"file"
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"---------------------- END ------------------------"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"## Metadata"
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|AUTHOR
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|CONTENT_LENGTH
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|DATE
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|KEYWORDS
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|LANGUAGE
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|TITLE
argument_list|)
expr_stmt|;
return|return
name|ExitStatus
operator|.
name|OK
return|;
block|}
DECL|method|printMetadataContent
specifier|private
name|void
name|printMetadataContent
parameter_list|(
name|ParseContext
operator|.
name|Document
name|doc
parameter_list|,
name|String
name|field
parameter_list|)
block|{
name|terminal
operator|.
name|println
argument_list|(
literal|"- %s: %s"
argument_list|,
name|field
argument_list|,
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
literal|"file."
operator|+
name|field
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|Command
name|parse
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|CommandLine
name|cli
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|url
init|=
name|cli
operator|.
name|getOptionValue
argument_list|(
literal|"u"
argument_list|)
decl_stmt|;
name|String
name|base64text
init|=
literal|null
decl_stmt|;
name|String
name|sSize
init|=
name|cli
operator|.
name|getOptionValue
argument_list|(
literal|"s"
argument_list|)
decl_stmt|;
name|Integer
name|size
init|=
name|sSize
operator|!=
literal|null
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|sSize
argument_list|)
else|:
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|url
operator|==
literal|null
operator|&&
name|cli
operator|.
name|getArgs
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|exitCmd
argument_list|(
name|ExitStatus
operator|.
name|USAGE
argument_list|,
name|terminal
argument_list|,
literal|"url or BASE64 content should be provided (type -h for help)"
argument_list|)
return|;
block|}
if|if
condition|(
name|url
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|cli
operator|.
name|getArgs
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|exitCmd
argument_list|(
name|ExitStatus
operator|.
name|USAGE
argument_list|,
name|terminal
argument_list|,
literal|"url or BASE64 content should be provided (type -h for help)"
argument_list|)
return|;
block|}
name|base64text
operator|=
name|cli
operator|.
name|getArgs
argument_list|()
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|cli
operator|.
name|getArgs
argument_list|()
operator|.
name|length
operator|==
literal|1
condition|)
block|{
return|return
name|exitCmd
argument_list|(
name|ExitStatus
operator|.
name|USAGE
argument_list|,
name|terminal
argument_list|,
literal|"url or BASE64 content should be provided. Not both. (type -h for help)"
argument_list|)
return|;
block|}
block|}
return|return
operator|new
name|TikaTest
argument_list|(
name|terminal
argument_list|,
name|url
argument_list|,
name|size
argument_list|,
name|base64text
argument_list|)
return|;
block|}
block|}
DECL|method|StandaloneTest
specifier|public
name|StandaloneTest
parameter_list|()
block|{
name|super
argument_list|(
name|CONFIG
argument_list|)
expr_stmt|;
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|StandaloneTest
name|pluginManager
init|=
operator|new
name|StandaloneTest
argument_list|()
decl_stmt|;
name|pluginManager
operator|.
name|execute
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|protected
name|Command
name|parse
parameter_list|(
name|String
name|cmdName
parameter_list|,
name|CommandLine
name|cli
parameter_list|)
throws|throws
name|Exception
block|{
switch|switch
condition|(
name|cmdName
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
condition|)
block|{
case|case
name|TikaTest
operator|.
name|NAME
case|:
return|return
name|TikaTest
operator|.
name|parse
argument_list|(
name|terminal
argument_list|,
name|cli
argument_list|)
return|;
default|default:
assert|assert
literal|false
operator|:
literal|"can't get here as cmd name is validated before this method is called"
assert|;
return|return
name|exitCmd
argument_list|(
name|ExitStatus
operator|.
name|CODE_ERROR
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

