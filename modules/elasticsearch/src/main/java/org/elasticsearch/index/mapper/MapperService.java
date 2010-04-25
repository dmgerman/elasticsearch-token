begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
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
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|UnmodifiableIterator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
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
name|env
operator|.
name|FailedToResolveConfigException
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
name|mapper
operator|.
name|json
operator|.
name|JsonDocumentMapperParser
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadSafe
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|Streams
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|MapBuilder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
annotation|@
name|ThreadSafe
DECL|class|MapperService
specifier|public
class|class
name|MapperService
extends|extends
name|AbstractIndexComponent
implements|implements
name|Iterable
argument_list|<
name|DocumentMapper
argument_list|>
block|{
comment|/**      * Will create types automatically if they do not exists in the repo yet      */
DECL|field|dynamic
specifier|private
specifier|final
name|boolean
name|dynamic
decl_stmt|;
DECL|field|dynamicMappingLocation
specifier|private
specifier|final
name|String
name|dynamicMappingLocation
decl_stmt|;
DECL|field|dynamicMappingUrl
specifier|private
specifier|final
name|URL
name|dynamicMappingUrl
decl_stmt|;
DECL|field|indexClassLoader
specifier|private
specifier|final
name|ClassLoader
name|indexClassLoader
decl_stmt|;
DECL|field|dynamicMappingSource
specifier|private
specifier|final
name|String
name|dynamicMappingSource
decl_stmt|;
DECL|field|mappers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|DocumentMapper
argument_list|>
name|mappers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|mutex
specifier|private
specifier|final
name|Object
name|mutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|nameFieldMappers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|FieldMappers
argument_list|>
name|nameFieldMappers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|indexNameFieldMappers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|FieldMappers
argument_list|>
name|indexNameFieldMappers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|fullNameFieldMappers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|FieldMappers
argument_list|>
name|fullNameFieldMappers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|idFieldMappers
specifier|private
specifier|volatile
name|FieldMappers
name|idFieldMappers
init|=
operator|new
name|FieldMappers
argument_list|()
decl_stmt|;
DECL|field|typeFieldMappers
specifier|private
specifier|volatile
name|FieldMappers
name|typeFieldMappers
init|=
operator|new
name|FieldMappers
argument_list|()
decl_stmt|;
DECL|field|uidFieldMappers
specifier|private
specifier|volatile
name|FieldMappers
name|uidFieldMappers
init|=
operator|new
name|FieldMappers
argument_list|()
decl_stmt|;
DECL|field|sourceFieldMappers
specifier|private
specifier|volatile
name|FieldMappers
name|sourceFieldMappers
init|=
operator|new
name|FieldMappers
argument_list|()
decl_stmt|;
comment|// for now, just use the json one. Can work on it more to support custom ones
DECL|field|documentParser
specifier|private
specifier|final
name|DocumentMapperParser
name|documentParser
decl_stmt|;
DECL|field|fieldMapperListener
specifier|private
specifier|final
name|InternalFieldMapperListener
name|fieldMapperListener
init|=
operator|new
name|InternalFieldMapperListener
argument_list|()
decl_stmt|;
DECL|field|searchAnalyzer
specifier|private
specifier|final
name|SmartIndexNameSearchAnalyzer
name|searchAnalyzer
decl_stmt|;
DECL|method|MapperService
annotation|@
name|Inject
specifier|public
name|MapperService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|Environment
name|environment
parameter_list|,
name|AnalysisService
name|analysisService
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
name|documentParser
operator|=
operator|new
name|JsonDocumentMapperParser
argument_list|(
name|analysisService
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchAnalyzer
operator|=
operator|new
name|SmartIndexNameSearchAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexClassLoader
operator|=
name|indexSettings
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
name|this
operator|.
name|dynamic
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"dynamic"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|dynamicMappingLocation
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"dynamic_mapping_location"
argument_list|)
decl_stmt|;
name|URL
name|dynamicMappingUrl
decl_stmt|;
if|if
condition|(
name|dynamicMappingLocation
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|dynamicMappingUrl
operator|=
name|environment
operator|.
name|resolveConfig
argument_list|(
literal|"dynamic-mapping.json"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedToResolveConfigException
name|e
parameter_list|)
block|{
comment|// not there, default to the built in one
name|dynamicMappingUrl
operator|=
name|indexClassLoader
operator|.
name|getResource
argument_list|(
literal|"org/elasticsearch/index/mapper/json/dynamic-mapping.json"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|dynamicMappingUrl
operator|=
name|environment
operator|.
name|resolveConfig
argument_list|(
name|dynamicMappingLocation
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedToResolveConfigException
name|e
parameter_list|)
block|{
comment|// not there, default to the built in one
try|try
block|{
name|dynamicMappingUrl
operator|=
operator|new
name|File
argument_list|(
name|dynamicMappingLocation
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|FailedToResolveConfigException
argument_list|(
literal|"Failed to resolve dynamic mapping location ["
operator|+
name|dynamicMappingLocation
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|this
operator|.
name|dynamicMappingUrl
operator|=
name|dynamicMappingUrl
expr_stmt|;
if|if
condition|(
name|dynamicMappingLocation
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|dynamicMappingLocation
operator|=
name|dynamicMappingUrl
operator|.
name|toExternalForm
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|dynamicMappingLocation
operator|=
name|dynamicMappingLocation
expr_stmt|;
block|}
if|if
condition|(
name|dynamic
condition|)
block|{
try|try
block|{
name|dynamicMappingSource
operator|=
name|Streams
operator|.
name|copyToString
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|dynamicMappingUrl
operator|.
name|openStream
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperException
argument_list|(
literal|"Failed to load default mapping source from ["
operator|+
name|dynamicMappingLocation
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|dynamicMappingSource
operator|=
literal|null
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"Using dynamic[{}] with location[{}] and source[{}]"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|dynamic
block|,
name|dynamicMappingLocation
block|,
name|dynamicMappingSource
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|UnmodifiableIterator
argument_list|<
name|DocumentMapper
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|mappers
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|type
specifier|public
name|DocumentMapper
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|DocumentMapper
name|mapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
return|return
name|mapper
return|;
block|}
if|if
condition|(
operator|!
name|dynamic
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// go ahead and dynamically create it
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|mapper
operator|=
name|mappers
operator|.
name|get
argument_list|(
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
return|return
name|mapper
return|;
block|}
name|add
argument_list|(
name|type
argument_list|,
name|dynamicMappingSource
argument_list|)
expr_stmt|;
return|return
name|mappers
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
block|}
DECL|method|documentMapperParser
specifier|public
name|DocumentMapperParser
name|documentMapperParser
parameter_list|()
block|{
return|return
name|this
operator|.
name|documentParser
return|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|mappingSource
parameter_list|)
block|{
name|add
argument_list|(
name|documentParser
operator|.
name|parse
argument_list|(
name|type
argument_list|,
name|mappingSource
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|String
name|mappingSource
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|add
argument_list|(
name|documentParser
operator|.
name|parse
argument_list|(
name|mappingSource
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Just parses and returns the mapper without adding it.      */
DECL|method|parse
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
name|String
name|mappingType
parameter_list|,
name|String
name|mappingSource
parameter_list|)
throws|throws
name|MapperParsingException
block|{
return|return
name|documentParser
operator|.
name|parse
argument_list|(
name|mappingType
argument_list|,
name|mappingSource
argument_list|)
return|;
block|}
DECL|method|hasMapping
specifier|public
name|boolean
name|hasMapping
parameter_list|(
name|String
name|mappingType
parameter_list|)
block|{
return|return
name|mappers
operator|.
name|containsKey
argument_list|(
name|mappingType
argument_list|)
return|;
block|}
DECL|method|documentMapper
specifier|public
name|DocumentMapper
name|documentMapper
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
name|mappers
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
DECL|method|idFieldMappers
specifier|public
name|FieldMappers
name|idFieldMappers
parameter_list|()
block|{
return|return
name|this
operator|.
name|idFieldMappers
return|;
block|}
DECL|method|typeFieldMappers
specifier|public
name|FieldMappers
name|typeFieldMappers
parameter_list|()
block|{
return|return
name|this
operator|.
name|typeFieldMappers
return|;
block|}
DECL|method|sourceFieldMappers
specifier|public
name|FieldMappers
name|sourceFieldMappers
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceFieldMappers
return|;
block|}
DECL|method|uidFieldMappers
specifier|public
name|FieldMappers
name|uidFieldMappers
parameter_list|()
block|{
return|return
name|this
operator|.
name|uidFieldMappers
return|;
block|}
comment|/**      * Returns {@link FieldMappers} for all the {@link FieldMapper}s that are registered      * under the given name across all the different {@link DocumentMapper} types.      *      * @param name The name to return all the {@link FieldMappers} for across all {@link DocumentMapper}s.      * @return All the {@link FieldMappers} for across all {@link DocumentMapper}s      */
DECL|method|name
specifier|public
name|FieldMappers
name|name
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|nameFieldMappers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**      * Returns {@link FieldMappers} for all the {@link FieldMapper}s that are registered      * under the given indexName across all the different {@link DocumentMapper} types.      *      * @param indexName The indexName to return all the {@link FieldMappers} for across all {@link DocumentMapper}s.      * @return All the {@link FieldMappers} across all {@link DocumentMapper}s for the given indexName.      */
DECL|method|indexName
specifier|public
name|FieldMappers
name|indexName
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
return|return
name|indexNameFieldMappers
operator|.
name|get
argument_list|(
name|indexName
argument_list|)
return|;
block|}
comment|/**      * Returns the {@link FieldMappers} of all the {@link FieldMapper}s that are      * registered under the give fullName across all the different {@link DocumentMapper} types.      *      * @param fullName The full name      * @return All teh {@link FieldMappers} across all the {@link DocumentMapper}s for the given fullName.      */
DECL|method|fullName
specifier|public
name|FieldMappers
name|fullName
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
return|return
name|fullNameFieldMappers
operator|.
name|get
argument_list|(
name|fullName
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #smartNameFieldMappers(String)} but returns the first field mapper for it. Returns      *<tt>null</tt> if there is none.      */
DECL|method|smartNameFieldMapper
specifier|public
name|FieldMapper
name|smartNameFieldMapper
parameter_list|(
name|String
name|smartName
parameter_list|)
block|{
name|FieldMappers
name|fieldMappers
init|=
name|smartNameFieldMappers
argument_list|(
name|smartName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
return|return
name|fieldMappers
operator|.
name|mapper
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Same as {@link #smartName(String)}, except it returns just the field mappers.      */
DECL|method|smartNameFieldMappers
specifier|public
name|FieldMappers
name|smartNameFieldMappers
parameter_list|(
name|String
name|smartName
parameter_list|)
block|{
name|int
name|dotIndex
init|=
name|smartName
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|dotIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|String
name|possibleType
init|=
name|smartName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dotIndex
argument_list|)
decl_stmt|;
name|DocumentMapper
name|possibleDocMapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|possibleType
argument_list|)
decl_stmt|;
if|if
condition|(
name|possibleDocMapper
operator|!=
literal|null
condition|)
block|{
name|String
name|possibleName
init|=
name|smartName
operator|.
name|substring
argument_list|(
name|dotIndex
operator|+
literal|1
argument_list|)
decl_stmt|;
name|FieldMappers
name|mappers
init|=
name|possibleDocMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
name|possibleName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
condition|)
block|{
return|return
name|mappers
return|;
block|}
block|}
block|}
name|FieldMappers
name|mappers
init|=
name|fullName
argument_list|(
name|smartName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
condition|)
block|{
return|return
name|mappers
return|;
block|}
return|return
name|indexName
argument_list|(
name|smartName
argument_list|)
return|;
block|}
comment|/**      * Returns smart field mappers based on a smart name. A smart name is one that can optioannly be prefixed      * with a type (and then a '.'). If it is, then the {@link MapperService.SmartNameFieldMappers}      * will have the doc mapper set.      *      *<p>It also (without the optional type prefix) try and find the {@link FieldMappers} for the specific      * name. It will first try to find it based on the full name (with the dots if its a compound name). If      * it is not found, will try and find it based on the indexName (which can be controlled in the mapping).      *      *<p>If nothing is found, returns null.      */
DECL|method|smartName
specifier|public
name|SmartNameFieldMappers
name|smartName
parameter_list|(
name|String
name|smartName
parameter_list|)
block|{
name|int
name|dotIndex
init|=
name|smartName
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|dotIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|String
name|possibleType
init|=
name|smartName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dotIndex
argument_list|)
decl_stmt|;
name|DocumentMapper
name|possibleDocMapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|possibleType
argument_list|)
decl_stmt|;
if|if
condition|(
name|possibleDocMapper
operator|!=
literal|null
condition|)
block|{
name|String
name|possibleName
init|=
name|smartName
operator|.
name|substring
argument_list|(
name|dotIndex
operator|+
literal|1
argument_list|)
decl_stmt|;
name|FieldMappers
name|mappers
init|=
name|possibleDocMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
name|possibleName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|SmartNameFieldMappers
argument_list|(
name|mappers
argument_list|,
name|possibleDocMapper
argument_list|)
return|;
block|}
block|}
block|}
name|FieldMappers
name|fieldMappers
init|=
name|fullName
argument_list|(
name|smartName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|SmartNameFieldMappers
argument_list|(
name|fieldMappers
argument_list|,
literal|null
argument_list|)
return|;
block|}
name|fieldMappers
operator|=
name|indexName
argument_list|(
name|smartName
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|SmartNameFieldMappers
argument_list|(
name|fieldMappers
argument_list|,
literal|null
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|DocumentMapper
name|mapper
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
if|if
condition|(
name|mapper
operator|.
name|type
argument_list|()
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'_'
condition|)
block|{
throw|throw
operator|new
name|InvalidTypeNameException
argument_list|(
literal|"Document mapping type name can't start with '_'"
argument_list|)
throw|;
block|}
name|mappers
operator|=
name|newMapBuilder
argument_list|(
name|mappers
argument_list|)
operator|.
name|put
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|mapper
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
name|mapper
operator|.
name|addFieldMapperListener
argument_list|(
name|fieldMapperListener
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|searchAnalyzer
specifier|public
name|Analyzer
name|searchAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchAnalyzer
return|;
block|}
DECL|class|SmartNameFieldMappers
specifier|public
specifier|static
class|class
name|SmartNameFieldMappers
block|{
DECL|field|fieldMappers
specifier|private
specifier|final
name|FieldMappers
name|fieldMappers
decl_stmt|;
DECL|field|docMapper
specifier|private
specifier|final
name|DocumentMapper
name|docMapper
decl_stmt|;
DECL|method|SmartNameFieldMappers
specifier|public
name|SmartNameFieldMappers
parameter_list|(
name|FieldMappers
name|fieldMappers
parameter_list|,
annotation|@
name|Nullable
name|DocumentMapper
name|docMapper
parameter_list|)
block|{
name|this
operator|.
name|fieldMappers
operator|=
name|fieldMappers
expr_stmt|;
name|this
operator|.
name|docMapper
operator|=
name|docMapper
expr_stmt|;
block|}
comment|/**          * Has at least one mapper for the field.          */
DECL|method|hasMapper
specifier|public
name|boolean
name|hasMapper
parameter_list|()
block|{
return|return
operator|!
name|fieldMappers
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**          * The first mapper for the smart named field.          */
DECL|method|mapper
specifier|public
name|FieldMapper
name|mapper
parameter_list|()
block|{
return|return
name|fieldMappers
operator|.
name|mapper
argument_list|()
return|;
block|}
comment|/**          * All the field mappers for the smart name field.          */
DECL|method|fieldMappers
specifier|public
name|FieldMappers
name|fieldMappers
parameter_list|()
block|{
return|return
name|fieldMappers
return|;
block|}
comment|/**          * If the smart name was a typed field, with a type that we resolved, will return          *<tt>true</tt>.          */
DECL|method|hasDocMapper
specifier|public
name|boolean
name|hasDocMapper
parameter_list|()
block|{
return|return
name|docMapper
operator|!=
literal|null
return|;
block|}
comment|/**          * If the smart name was a typed field, with a type that we resolved, will return          * the document mapper for it.          */
DECL|method|docMapper
specifier|public
name|DocumentMapper
name|docMapper
parameter_list|()
block|{
return|return
name|docMapper
return|;
block|}
block|}
DECL|class|SmartIndexNameSearchAnalyzer
specifier|private
class|class
name|SmartIndexNameSearchAnalyzer
extends|extends
name|Analyzer
block|{
DECL|field|defaultAnalyzer
specifier|private
specifier|final
name|Analyzer
name|defaultAnalyzer
decl_stmt|;
DECL|method|SmartIndexNameSearchAnalyzer
specifier|private
name|SmartIndexNameSearchAnalyzer
parameter_list|(
name|Analyzer
name|defaultAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|defaultAnalyzer
operator|=
name|defaultAnalyzer
expr_stmt|;
block|}
DECL|method|tokenStream
annotation|@
name|Override
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Reader
name|reader
parameter_list|)
block|{
name|int
name|dotIndex
init|=
name|fieldName
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|dotIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|String
name|possibleType
init|=
name|fieldName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dotIndex
argument_list|)
decl_stmt|;
name|DocumentMapper
name|possibleDocMapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|possibleType
argument_list|)
decl_stmt|;
if|if
condition|(
name|possibleDocMapper
operator|!=
literal|null
condition|)
block|{
return|return
name|possibleDocMapper
operator|.
name|mappers
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|.
name|tokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
name|FieldMappers
name|mappers
init|=
name|fullNameFieldMappers
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|.
name|tokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
name|mappers
operator|=
name|indexNameFieldMappers
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|.
name|tokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
return|return
name|defaultAnalyzer
operator|.
name|tokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
DECL|method|reusableTokenStream
annotation|@
name|Override
specifier|public
name|TokenStream
name|reusableTokenStream
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|dotIndex
init|=
name|fieldName
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|dotIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|String
name|possibleType
init|=
name|fieldName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dotIndex
argument_list|)
decl_stmt|;
name|DocumentMapper
name|possibleDocMapper
init|=
name|mappers
operator|.
name|get
argument_list|(
name|possibleType
argument_list|)
decl_stmt|;
if|if
condition|(
name|possibleDocMapper
operator|!=
literal|null
condition|)
block|{
return|return
name|possibleDocMapper
operator|.
name|mappers
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|.
name|reusableTokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
name|FieldMappers
name|mappers
init|=
name|fullNameFieldMappers
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|.
name|reusableTokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
name|mappers
operator|=
name|indexNameFieldMappers
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|mappers
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|!=
literal|null
operator|&&
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|mappers
operator|.
name|mapper
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|.
name|reusableTokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
return|return
name|defaultAnalyzer
operator|.
name|reusableTokenStream
argument_list|(
name|fieldName
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
DECL|class|InternalFieldMapperListener
specifier|private
class|class
name|InternalFieldMapperListener
implements|implements
name|FieldMapperListener
block|{
DECL|method|fieldMapper
annotation|@
name|Override
specifier|public
name|void
name|fieldMapper
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
if|if
condition|(
name|fieldMapper
operator|instanceof
name|IdFieldMapper
condition|)
block|{
name|idFieldMappers
operator|=
name|idFieldMappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldMapper
operator|instanceof
name|TypeFieldMapper
condition|)
block|{
name|typeFieldMappers
operator|=
name|typeFieldMappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldMapper
operator|instanceof
name|SourceFieldMapper
condition|)
block|{
name|sourceFieldMappers
operator|=
name|sourceFieldMappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldMapper
operator|instanceof
name|UidFieldMapper
condition|)
block|{
name|uidFieldMappers
operator|=
name|uidFieldMappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
name|FieldMappers
name|mappers
init|=
name|nameFieldMappers
operator|.
name|get
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappers
operator|==
literal|null
condition|)
block|{
name|mappers
operator|=
operator|new
name|FieldMappers
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mappers
operator|=
name|mappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
name|nameFieldMappers
operator|=
name|newMapBuilder
argument_list|(
name|nameFieldMappers
argument_list|)
operator|.
name|put
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|mappers
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
name|mappers
operator|=
name|indexNameFieldMappers
operator|.
name|get
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|mappers
operator|==
literal|null
condition|)
block|{
name|mappers
operator|=
operator|new
name|FieldMappers
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mappers
operator|=
name|mappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
name|indexNameFieldMappers
operator|=
name|newMapBuilder
argument_list|(
name|indexNameFieldMappers
argument_list|)
operator|.
name|put
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|mappers
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
name|mappers
operator|=
name|fullNameFieldMappers
operator|.
name|get
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|mappers
operator|==
literal|null
condition|)
block|{
name|mappers
operator|=
operator|new
name|FieldMappers
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mappers
operator|=
name|mappers
operator|.
name|concat
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
name|fullNameFieldMappers
operator|=
name|newMapBuilder
argument_list|(
name|fullNameFieldMappers
argument_list|)
operator|.
name|put
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|,
name|mappers
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

