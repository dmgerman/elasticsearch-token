begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mapper.attachments
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
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
name|SuppressForbidden
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
name|compress
operator|.
name|CompressedXContent
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
name|PathUtils
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
name|BytesStreamOutput
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
name|MapperTestUtils
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
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|copy
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentUnitTestCase
operator|.
name|getIndicesModuleWithRegisteredAttachmentMapper
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
import|;
end_import

begin_comment
comment|/**  * This class provides a simple main class which can be used to test what is extracted from a given binary file.  * You can run it using  *  -u file://URL/TO/YOUR/DOC  *  --size set extracted size (default to mapper attachment size)  *  BASE64 encoded binary  *  * Example:  *  StandaloneRunner BASE64Text  *  StandaloneRunner -u /tmp/mydoc.pdf  *  StandaloneRunner -u /tmp/mydoc.pdf --size 1000000  */
end_comment

begin_class
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"commandline tool"
argument_list|)
DECL|class|StandaloneRunner
specifier|public
class|class
name|StandaloneRunner
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
name|StandaloneRunner
operator|.
name|class
argument_list|)
operator|.
name|cmds
argument_list|(
name|TikaRunner
operator|.
name|CMD
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
static|static
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.path.home"
argument_list|,
literal|"/tmp"
argument_list|)
expr_stmt|;
block|}
DECL|class|TikaRunner
specifier|static
class|class
name|TikaRunner
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
name|TikaRunner
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
literal|"t"
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
DECL|method|TikaRunner
specifier|protected
name|TikaRunner
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
name|newMapperService
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
literal|"."
argument_list|)
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|getIndicesModuleWithRegisteredAttachmentMapper
argument_list|()
argument_list|)
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
comment|// use CWD b/c it won't be used
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/standalone/standalone-mapping.json"
argument_list|)
decl_stmt|;
name|docMapper
operator|=
name|mapperParser
operator|.
name|parse
argument_list|(
literal|"person"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
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
name|byte
index|[]
name|bytes
init|=
name|copyToBytes
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
name|url
argument_list|)
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
literal|"person"
argument_list|,
literal|"person"
argument_list|,
literal|"1"
argument_list|,
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
literal|"file.content"
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
name|getMapper
argument_list|(
literal|"file."
operator|+
name|field
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|copyToBytes
specifier|public
specifier|static
name|byte
index|[]
name|copyToBytes
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|InputStream
name|is
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|path
argument_list|)
init|)
block|{
if|if
condition|(
name|is
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Resource ["
operator|+
name|path
operator|+
literal|"] not found in classpath"
argument_list|)
throw|;
block|}
try|try
init|(
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|copy
argument_list|(
name|is
argument_list|,
name|out
argument_list|)
expr_stmt|;
return|return
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|toBytes
argument_list|()
return|;
block|}
block|}
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
literal|"size"
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
name|TikaRunner
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
DECL|method|StandaloneRunner
specifier|public
name|StandaloneRunner
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
name|StandaloneRunner
name|pluginManager
init|=
operator|new
name|StandaloneRunner
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
name|TikaRunner
operator|.
name|NAME
case|:
return|return
name|TikaRunner
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

