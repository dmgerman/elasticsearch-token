begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.lookup
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
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
name|LeafReaderContext
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
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldDataService
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
name|ScriptDocValues
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
name|MappedFieldType
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
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|LeafDocLookup
specifier|public
class|class
name|LeafDocLookup
implements|implements
name|Map
block|{
DECL|field|localCacheFieldData
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptDocValues
argument_list|>
name|localCacheFieldData
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|4
argument_list|)
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|fieldDataService
specifier|private
specifier|final
name|IndexFieldDataService
name|fieldDataService
decl_stmt|;
annotation|@
name|Nullable
DECL|field|types
specifier|private
specifier|final
name|String
index|[]
name|types
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|LeafReaderContext
name|reader
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|LeafDocLookup
name|LeafDocLookup
parameter_list|(
name|MapperService
name|mapperService
parameter_list|,
name|IndexFieldDataService
name|fieldDataService
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|types
parameter_list|,
name|LeafReaderContext
name|reader
parameter_list|)
block|{
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|fieldDataService
operator|=
name|fieldDataService
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
block|}
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|this
operator|.
name|mapperService
return|;
block|}
DECL|method|fieldDataService
specifier|public
name|IndexFieldDataService
name|fieldDataService
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldDataService
return|;
block|}
DECL|method|setDocument
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
comment|// assume its a string...
name|String
name|fieldName
init|=
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
name|ScriptDocValues
name|scriptValues
init|=
name|localCacheFieldData
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValues
operator|==
literal|null
condition|)
block|{
specifier|final
name|MappedFieldType
name|fieldType
init|=
name|mapperService
operator|.
name|fullName
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No field found for ["
operator|+
name|fieldName
operator|+
literal|"] in mapping with types "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|types
argument_list|)
operator|+
literal|""
argument_list|)
throw|;
block|}
comment|// load fielddata on behalf of the script: otherwise it would need additional permissions
comment|// to deal with pagedbytes/ramusagestimator/etc
name|scriptValues
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|ScriptDocValues
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ScriptDocValues
name|run
parameter_list|()
block|{
return|return
name|fieldDataService
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
operator|.
name|load
argument_list|(
name|reader
argument_list|)
operator|.
name|getScriptValues
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|localCacheFieldData
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|scriptValues
argument_list|)
expr_stmt|;
block|}
name|scriptValues
operator|.
name|setNextDocId
argument_list|(
name|docId
argument_list|)
expr_stmt|;
return|return
name|scriptValues
return|;
block|}
annotation|@
name|Override
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
comment|// assume its a string...
name|String
name|fieldName
init|=
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
name|ScriptDocValues
name|scriptValues
init|=
name|localCacheFieldData
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValues
operator|==
literal|null
condition|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|mapperService
operator|.
name|fullName
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|put
specifier|public
name|Object
name|put
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
name|Object
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
name|m
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|keySet
specifier|public
name|Set
name|keySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|values
specifier|public
name|Collection
name|values
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|entrySet
specifier|public
name|Set
name|entrySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

