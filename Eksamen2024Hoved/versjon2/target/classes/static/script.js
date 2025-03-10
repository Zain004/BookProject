// @ts-ignore
import React, { useState } from "react";
// @ts-ignore
import axios from 'axios';

export const validateField = (value, regex, inputField) => {
    const validateInput = document.getElementById('inputField');
    if(!regex.test(value)) {
        validateInput.classList.add('is-invalid');
        return false;
    }
    validateInput.classList.remove('is-invalid');
    validateInput.classList.add('is-valid');
    return true;
};
export const validateFirstname = () => {
    const firstName = document.getElementById('firstname');
    return validateField(
        firstName.value,
        /^(?!\s)(?!.*\s{2})[A-Za-z.\- ]{2,50}(?<!\s)$/,
        firstName
    );
};
export const validateLastname = () => {
    const lastname = document.getElementById('lastname');
    return validateField(
        lastname.value,
        /^(?!\s)(?!.*\s{2})[A-Za-z.\- ]{2,50}(?<!\s)$/,
        lastname
    );
};
export const validateDob = () => {
    const dobInput = document.getElementById('dob'); // Hent input-feltet
    const invalidFeedback = dobInput.nextElementSibling; // hent tilknyttet neste element som er invalid-feedback
    const dobValue = dobInput.value; // Hent verdien direkte fra input-feltet
    // sjekk om datoen er tom
    if(!dobValue) {
        dobInput.classList.add("is-invalid");
        invalidFeedback.textContent = "Please enter your date of Birth";
        return false;
    }
    const dob = new Date(dobInput.value); // lag et nytt Date objekt
    const today = new Date(); // finn dagens dato
    let age = today.getFullYear() - dob.getFullYear(); // beregn
    const monthDifference = today.getMonth() - dob.getMonth();

    if(monthDifference < 0 || (monthDifference === 0 && today.getDate() < dob.getDate())) {
        age--;
    }
    // Sjekk om brukeren er minst 18 år
    if(age < 18) {
        dobInput.classList.add('is-invalid'); // legg til feilmeldingen
        invalidFeedback.textContent = "You must be at least 18 years old.";
        return false;
    }
    // fjern is-invalid og legg til is-valid (grønn kant)
    dobInput.classList.remove('is-invalid');
    dobInput.classList.add('is-valid'); // legg til is-valid (grønn-kant)
    return true;
};
export const validatePhone = () => {
    const phone = document.getElementById('phone');
    return validateField(
        phone.value,
        /^(?!\s)[0-9]{8}(?<!\s)$/,
        phone
    );
};
export const validateEmail = () => {
    const email = document.getElementById('email');
    return validateField(
        email.value,
        /^[A-Za-z0-9._%+/-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/,
        email
    );
};
interface FormState {
    firstName: string;
    lastName: string;
    dob: Date;
    phone: string;
    email: string;
}
// definerer en funksjon med React
const RegisterForm: React.FC = ()=> {
    // const [formstate, setFormState] er syntaksen som bruker destrukturering
    // for å hente ut verdiene fra arrayen som returneres fra useState.
    const[formstate, setFormState] = useState<FormState>({ // useState brukes for å hente verdiene fra form
        // og sammenligne disse med interfacet for å sikre riktig type
        firstName:'',
        lastName:'',
        dob: new Date(),
        phone:'',
        email:''
    });
    // toastMessage er en String
    // setToastMEssage brukes for å oppdatere verdien
    // useState kan enten inneholde en String eller null og den initiale
    // verdien av toastMessage er null i starten
    const[toastMessage, setToastMessage] = useState<String | null>(null);
    const[toastType, setToastType] = useState<'success' | 'error' | null>(null);

    // den tar inn en event
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormState((prevState) => ({
            ...prevState,  // Beholder de gamle verdiene
            [name]: value  // Oppdaterer den spesifikke egenskapen med den nye verdien
        }));
    };

    // @ts-ignore
    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>)=> {
        e.preventDefault();
        const {firstName, lastName, dob, phone, email} = formstate;
        // sjekk validering om feltene er riktig valdiert
        if (!validateFirstname() || !validateLastname() || !validateDob() ||
            !validatePhone() || !validateEmail()) {
            showToast('Please fill in all fields correctly.', false);
        }
        try {
            const response = await registerUser(formstate);
            console.info(formstate);
            console.info(response?.data?.message);
            showToast('Registration succssfull', true);
        } catch (error: any) {
            console.error('Registration failed:',error);
            showToast(error.response?.data?.message || 'Something went wrong!', false);
        }
    }
}
interface RegisterUserData {
    firstName: string;
    lastName: string;
    dob: Date;
    phone: string;
    email: string;
}
// export brukes for å synliggjøre funksjonen utenfor filen de er definert i
export const registerUser = async (data: RegisterUserData) => {
    const response = await axios.post('/saveUser',data);
    return response.data;
}