package com.abietic.ap1.service.model;

// import javax.validation.constraints.Max;
// import javax.validation.constraints.Min;
// import org.hibernate.validator.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
// import javax.validation.constraints.NotNull;

public class UserModel implements org.springframework.security.core.userdetails.UserDetails {
    private static final long serialVersionUID = 1L;
    private Integer id;
    // @NotBlank(message = "用户名不能为空")
    @NotNull(message = "用户名不能为空")
    private String name;

    @NotNull(message = "性别必须填写")
    private Byte gender;

    @NotNull(message = "年龄必须填写")
    @Min(value = 0, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄必须小于150")
    private Integer age;

    // @NotBlank(message = "手机号不能为空")
    @NotNull(message = "手机号不能为空")
    private String telphone;
    private String registerMode;
    private String thirdPartyId;

    // @NotBlank(message = "密码不能为空")
    @NotNull(message = "密码不能为空")
    private String encrptPassword;

    private Set<GrantedAuthority> authorities;

    public String getEncrptPassword() {
        return encrptPassword;
    }

    public void setEncrptPassword(String encrptPassword) {
        this.encrptPassword = encrptPassword;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public void defineAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities));
    }

    // 从spring实现的userdetails.User中复制来方法
    private static SortedSet<GrantedAuthority> sortAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());

        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority,
                    "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }

        return sortedAuthorities;
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>,
            Serializable {
        private static final long serialVersionUID = -1;

        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            // Neither should ever be null as each entry is checked before adding it to
            // the set.
            // If the authority is null, it is a custom authority and should precede
            // others.
            if (g2.getAuthority() == null) {
                return -1;
            }

            if (g1.getAuthority() == null) {
                return 1;
            }

            return g1.getAuthority().compareTo(g2.getAuthority());
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public void setAuthorities(Set<GrantedAuthority> authorities) {
        // 配合json反序列化
    }

    @Override
    public String getPassword() {
        return "Should not save password here!";
    }

    public void setPassword(String password) {
        // 配合json反序列化
    }

    @Override
    public String getUsername() {
        // 这里返回
        return telphone;
    }

    public void setUsername(String userName) {
        // 配合json反序列化
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        // 配合json反序列化
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        // 配合json反序列化
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        // 配合json反序列化
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(boolean enabled) {
        // 配合json反序列化
    }

    

}
