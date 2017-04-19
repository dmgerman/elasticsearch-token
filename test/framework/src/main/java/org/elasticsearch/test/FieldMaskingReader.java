begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|DirectoryReader
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
name|index
operator|.
name|FieldFilterLeafReader
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
name|index
operator|.
name|FilterDirectoryReader
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
name|index
operator|.
name|FilterLeafReader
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
name|index
operator|.
name|LeafReader
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
name|index
operator|.
name|IndexReader
operator|.
name|CacheHelper
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
name|Collections
import|;
end_import

begin_class
DECL|class|FieldMaskingReader
specifier|public
class|class
name|FieldMaskingReader
extends|extends
name|FilterDirectoryReader
block|{
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|method|FieldMaskingReader
specifier|public
name|FieldMaskingReader
parameter_list|(
name|String
name|field
parameter_list|,
name|DirectoryReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
operator|new
name|FilterDirectoryReader
operator|.
name|SubReaderWrapper
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LeafReader
name|wrap
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
block|{
return|return
operator|new
name|FilterLeafReader
argument_list|(
operator|new
name|FieldFilterLeafReader
argument_list|(
name|reader
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|field
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
block|{
comment|// FieldFilterLeafReader does not forward cache helpers
comment|// since it considers it is illegal because of the fact
comment|// that it changes the content of the index. However we
comment|// want this behavior for tests, and security plugins
comment|// are careful to only use the cache when it's valid
annotation|@
name|Override
specifier|public
name|CacheHelper
name|getReaderCacheHelper
parameter_list|()
block|{
return|return
name|reader
operator|.
name|getReaderCacheHelper
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|CacheHelper
name|getCoreCacheHelper
parameter_list|()
block|{
return|return
name|reader
operator|.
name|getCoreCacheHelper
argument_list|()
return|;
block|}
block|}
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWrapDirectoryReader
specifier|protected
name|DirectoryReader
name|doWrapDirectoryReader
parameter_list|(
name|DirectoryReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FieldMaskingReader
argument_list|(
name|field
argument_list|,
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getReaderCacheHelper
specifier|public
name|CacheHelper
name|getReaderCacheHelper
parameter_list|()
block|{
return|return
name|in
operator|.
name|getReaderCacheHelper
argument_list|()
return|;
block|}
block|}
end_class

end_unit

