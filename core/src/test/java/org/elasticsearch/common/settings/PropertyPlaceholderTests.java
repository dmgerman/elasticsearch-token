begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_class
DECL|class|PropertyPlaceholderTests
specifier|public
class|class
name|PropertyPlaceholderTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"{"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo1"
argument_list|,
literal|"bar1"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo2"
argument_list|,
literal|"bar2"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bar1"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"{foo1}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a bar1b"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"a {foo1}b"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar1bar2"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"{foo1}{foo2}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a bar1 b bar2 c"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"a {foo1} b {foo2} c"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testVariousPrefixSuffix
specifier|public
name|void
name|testVariousPrefixSuffix
parameter_list|()
block|{
comment|// Test various prefix/suffix lengths
name|PropertyPlaceholder
name|ppEqualsPrefix
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"{"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|PropertyPlaceholder
name|ppLongerPrefix
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|PropertyPlaceholder
name|ppShorterPrefix
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"{"
argument_list|,
literal|"}}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|ppEqualsPrefix
operator|.
name|replacePlaceholders
argument_list|(
literal|"{foo}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|ppLongerPrefix
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|ppShorterPrefix
operator|.
name|replacePlaceholders
argument_list|(
literal|"{foo}}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDefaultValue
specifier|public
name|void
name|testDefaultValue
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo:bar}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo:}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIgnoredUnresolvedPlaceholder
specifier|public
name|void
name|testIgnoredUnresolvedPlaceholder
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"${foo}"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNotIgnoredUnresolvedPlaceholder
specifier|public
name|void
name|testNotIgnoredUnresolvedPlaceholder
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"Could not resolve placeholder 'foo'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testShouldIgnoreMissing
specifier|public
name|void
name|testShouldIgnoreMissing
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"bar${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRecursive
specifier|public
name|void
name|testRecursive
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"${foo1}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo1"
argument_list|,
literal|"${foo2}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo2"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"abarb"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"a${foo}b"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNestedLongerPrefix
specifier|public
name|void
name|testNestedLongerPrefix
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"${foo1}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo1"
argument_list|,
literal|"${foo2}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo2"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"barbar"
argument_list|,
literal|"baz"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"baz"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${bar${foo}}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNestedSameLengthPrefixSuffix
specifier|public
name|void
name|testNestedSameLengthPrefixSuffix
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"{"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"{foo1}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo1"
argument_list|,
literal|"{foo2}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo2"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"barbar"
argument_list|,
literal|"baz"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"baz"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"{bar{foo}}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNestedShorterPrefix
specifier|public
name|void
name|testNestedShorterPrefix
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"{"
argument_list|,
literal|"}}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"{foo1}}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo1"
argument_list|,
literal|"{foo2}}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo2"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"barbar"
argument_list|,
literal|"baz"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"baz"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"{bar{foo}}}}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCircularReference
specifier|public
name|void
name|testCircularReference
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"${bar}"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
literal|"${foo}"
argument_list|)
expr_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"Circular placeholder reference 'foo' in property definitions"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testShouldRemoveMissing
specifier|public
name|void
name|testShouldRemoveMissing
parameter_list|()
block|{
name|PropertyPlaceholder
name|propertyPlaceholder
init|=
operator|new
name|PropertyPlaceholder
argument_list|(
literal|"${"
argument_list|,
literal|"}"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
name|placeholderResolver
init|=
operator|new
name|SimplePlaceholderResolver
argument_list|(
name|map
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bar${foo}"
argument_list|,
name|propertyPlaceholder
operator|.
name|replacePlaceholders
argument_list|(
literal|"bar${foo}"
argument_list|,
name|placeholderResolver
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|SimplePlaceholderResolver
specifier|private
class|class
name|SimplePlaceholderResolver
implements|implements
name|PropertyPlaceholder
operator|.
name|PlaceholderResolver
block|{
DECL|field|map
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
decl_stmt|;
DECL|field|shouldIgnoreMissing
specifier|private
name|boolean
name|shouldIgnoreMissing
decl_stmt|;
DECL|field|shouldRemoveMissing
specifier|private
name|boolean
name|shouldRemoveMissing
decl_stmt|;
DECL|method|SimplePlaceholderResolver
name|SimplePlaceholderResolver
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|,
name|boolean
name|shouldIgnoreMissing
parameter_list|,
name|boolean
name|shouldRemoveMissing
parameter_list|)
block|{
name|this
operator|.
name|map
operator|=
name|map
expr_stmt|;
name|this
operator|.
name|shouldIgnoreMissing
operator|=
name|shouldIgnoreMissing
expr_stmt|;
name|this
operator|.
name|shouldRemoveMissing
operator|=
name|shouldRemoveMissing
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|resolvePlaceholder
specifier|public
name|String
name|resolvePlaceholder
parameter_list|(
name|String
name|placeholderName
parameter_list|)
block|{
return|return
name|map
operator|.
name|get
argument_list|(
name|placeholderName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shouldIgnoreMissing
specifier|public
name|boolean
name|shouldIgnoreMissing
parameter_list|(
name|String
name|placeholderName
parameter_list|)
block|{
return|return
name|shouldIgnoreMissing
return|;
block|}
annotation|@
name|Override
DECL|method|shouldRemoveMissingPlaceholder
specifier|public
name|boolean
name|shouldRemoveMissingPlaceholder
parameter_list|(
name|String
name|placeholderName
parameter_list|)
block|{
return|return
name|shouldRemoveMissing
return|;
block|}
block|}
block|}
end_class

end_unit

