begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch
package|package
name|org
operator|.
name|elasticsearch
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
name|base
operator|.
name|Joiner
import|;
end_import

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
name|Sets
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchLuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchTokenStreamTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|*
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
name|attribute
operator|.
name|BasicFileAttributes
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
comment|/**  * Simple class that ensures that all subclasses concrete of ElasticsearchTestCase end with either Test | Tests  */
end_comment

begin_class
DECL|class|NamingConventionTests
specifier|public
class|class
name|NamingConventionTests
extends|extends
name|ElasticsearchTestCase
block|{
comment|// see https://github.com/elasticsearch/elasticsearch/issues/9945
DECL|method|testNamingConventions
specifier|public
name|void
name|testNamingConventions
parameter_list|()
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
throws|,
name|URISyntaxException
block|{
specifier|final
name|Set
argument_list|<
name|Class
argument_list|>
name|notImplementing
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Class
argument_list|>
name|pureUnitTest
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Class
argument_list|>
name|missingSuffix
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|packages
init|=
block|{
literal|"org.elasticsearch"
block|,
literal|"org.apache.lucene"
block|}
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|packageName
range|:
name|packages
control|)
block|{
specifier|final
name|String
name|path
init|=
literal|"/"
operator|+
name|packageName
operator|.
name|replace
argument_list|(
literal|'.'
argument_list|,
literal|'/'
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|startPath
init|=
name|Paths
operator|.
name|get
argument_list|(
name|NamingConventionTests
operator|.
name|class
operator|.
name|getResource
argument_list|(
name|path
argument_list|)
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Path
argument_list|>
name|ignore
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
literal|"/org/elasticsearch/stresstest"
argument_list|)
argument_list|,
name|Paths
operator|.
name|get
argument_list|(
literal|"/org/elasticsearch/benchmark/stress"
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|walkFileTree
argument_list|(
name|startPath
argument_list|,
operator|new
name|FileVisitor
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
specifier|private
name|Path
name|pkgPrefix
init|=
name|Paths
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|.
name|getParent
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|preVisitDirectory
parameter_list|(
name|Path
name|dir
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|next
init|=
name|pkgPrefix
operator|.
name|resolve
argument_list|(
name|dir
operator|.
name|getFileName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ignore
operator|.
name|contains
argument_list|(
name|next
argument_list|)
condition|)
block|{
return|return
name|FileVisitResult
operator|.
name|SKIP_SUBTREE
return|;
block|}
name|pkgPrefix
operator|=
name|next
expr_stmt|;
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|String
name|filename
init|=
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|filename
operator|.
name|endsWith
argument_list|(
literal|".class"
argument_list|)
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|loadClass
argument_list|(
name|filename
argument_list|)
decl_stmt|;
if|if
condition|(
name|Modifier
operator|.
name|isAbstract
argument_list|(
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|==
literal|false
operator|&&
name|Modifier
operator|.
name|isInterface
argument_list|(
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
if|if
condition|(
operator|(
name|clazz
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"Tests"
argument_list|)
operator|||
name|clazz
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"Test"
argument_list|)
operator|)
condition|)
block|{
comment|// don't worry about the ones that match the pattern
if|if
condition|(
name|isTestCase
argument_list|(
name|clazz
argument_list|)
operator|==
literal|false
condition|)
block|{
name|notImplementing
operator|.
name|add
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|isTestCase
argument_list|(
name|clazz
argument_list|)
condition|)
block|{
name|missingSuffix
operator|.
name|add
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|junit
operator|.
name|framework
operator|.
name|Test
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
operator|||
name|hasTestAnnotation
argument_list|(
name|clazz
argument_list|)
condition|)
block|{
name|pureUnitTest
operator|.
name|add
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
specifier|private
name|boolean
name|hasTestAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
for|for
control|(
name|Method
name|method
range|:
name|clazz
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|method
operator|.
name|getAnnotation
argument_list|(
name|Test
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|isTestCase
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
name|ElasticsearchTestCase
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
operator|||
name|ElasticsearchLuceneTestCase
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
operator|||
name|ElasticsearchTokenStreamTestCase
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
operator|||
name|LuceneTestCase
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
return|;
block|}
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|filename
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
name|StringBuilder
name|pkg
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|p
range|:
name|pkgPrefix
control|)
block|{
name|pkg
operator|.
name|append
argument_list|(
name|p
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
block|}
name|pkg
operator|.
name|append
argument_list|(
name|filename
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|filename
operator|.
name|length
argument_list|()
operator|-
literal|6
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Class
operator|.
name|forName
argument_list|(
name|pkg
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFileFailed
parameter_list|(
name|Path
name|file
parameter_list|,
name|IOException
name|exc
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
name|exc
throw|;
block|}
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|postVisitDirectory
parameter_list|(
name|Path
name|dir
parameter_list|,
name|IOException
name|exc
parameter_list|)
throws|throws
name|IOException
block|{
name|pkgPrefix
operator|=
name|pkgPrefix
operator|.
name|getParent
argument_list|()
expr_stmt|;
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|missingSuffix
operator|.
name|remove
argument_list|(
name|WrongName
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|missingSuffix
operator|.
name|remove
argument_list|(
name|WrongNameTheSecond
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|notImplementing
operator|.
name|remove
argument_list|(
name|NotImplementingTests
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|notImplementing
operator|.
name|remove
argument_list|(
name|NotImplementingTest
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|pureUnitTest
operator|.
name|remove
argument_list|(
name|PlainUnit
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|pureUnitTest
operator|.
name|remove
argument_list|(
name|PlainUnitTheSecond
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|classesToSubclass
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|join
argument_list|(
name|ElasticsearchTestCase
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|ElasticsearchLuceneTestCase
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|ElasticsearchTokenStreamTestCase
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|LuceneTestCase
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Not all subclasses of "
operator|+
name|ElasticsearchTestCase
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" match the naming convention. Concrete classes must end with [Test|Tests]: "
operator|+
name|missingSuffix
operator|.
name|toString
argument_list|()
argument_list|,
name|missingSuffix
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Pure Unit-Test found must subclass one of ["
operator|+
name|classesToSubclass
operator|+
literal|"] "
operator|+
name|pureUnitTest
operator|.
name|toString
argument_list|()
argument_list|,
name|pureUnitTest
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Classes ending with Test|Tests] must subclass ["
operator|+
name|classesToSubclass
operator|+
literal|"] "
operator|+
name|notImplementing
operator|.
name|toString
argument_list|()
argument_list|,
name|notImplementing
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*      * Some test the test classes      */
annotation|@
name|Ignore
DECL|class|NotImplementingTests
specifier|public
specifier|static
specifier|final
class|class
name|NotImplementingTests
block|{}
annotation|@
name|Ignore
DECL|class|NotImplementingTest
specifier|public
specifier|static
specifier|final
class|class
name|NotImplementingTest
block|{}
DECL|class|WrongName
specifier|public
specifier|static
specifier|final
class|class
name|WrongName
extends|extends
name|ElasticsearchTestCase
block|{}
DECL|class|WrongNameTheSecond
specifier|public
specifier|static
specifier|final
class|class
name|WrongNameTheSecond
extends|extends
name|ElasticsearchLuceneTestCase
block|{}
DECL|class|PlainUnit
specifier|public
specifier|static
specifier|final
class|class
name|PlainUnit
extends|extends
name|TestCase
block|{}
DECL|class|PlainUnitTheSecond
specifier|public
specifier|static
specifier|final
class|class
name|PlainUnitTheSecond
block|{
annotation|@
name|Test
DECL|method|foo
specifier|public
name|void
name|foo
parameter_list|()
block|{         }
block|}
block|}
end_class

end_unit

